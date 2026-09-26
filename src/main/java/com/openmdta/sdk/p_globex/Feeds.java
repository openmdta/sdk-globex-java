package com.openmdta.sdk.p_globex;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.*;

/** Sink workflows. Sink methods are synchronous and finish before credits are replenished. */
public final class Feeds {
    private Feeds() {}
    public record Gap(long after, long through) {
        public Gap { if (Long.compareUnsigned(after, through) >= 0) throw new IllegalArgumentException("Invalid gap"); }
    }
    public record Resume(long through, List<Gap> gaps, boolean initialized) {
        public Resume { gaps = List.copyOf(gaps); }
    }
    public interface StreamSink {
        Resume resume() throws Exception;
        /** Commit each source message atomically. Ignore older/duplicate values per record AND block. */
        void write(BatchView batch) throws Exception;
        /** Atomically persist coverage, open gaps and bootstrap state, after all preceding data. */
        void checkpoint(Resume state) throws Exception;
    }
    public interface CatalogSink {
        String resume() throws Exception;
        /** Abandon an interrupted staging snapshot, retaining committed data and cursor. */
        void reset() throws Exception;
        void snapshotBegin() throws Exception;
        /** Borrowed typed view. Apply whole-record replacements/deletions keyed by recordKey. */
        void write(CatalogRecord record) throws Exception;
        /** Atomically swap the staged snapshot and persist its cursor. */
        void snapshotComplete(String cursor) throws Exception;
        /** Persist only after preceding writes have committed. */
        void cursor(String cursor) throws Exception;
    }
    public static final class Stream implements AutoCloseable {
        private final Dataset dataset;
        private final List<? extends Block<?>> blocks;
        private final StreamSink sink;
        private final BatchView batch = new BatchView();
        private final FeedControlDecoder control = new FeedControlDecoder();
        private final FeedSnapshotHeaderDecoder snapshot = new FeedSnapshotHeaderDecoder();
        private final UnsafeBuffer text = new UnsafeBuffer(0, 0);
        private final ArrayList<Gap> gaps = new ArrayList<>();
        private final ArrayList<Request> active = new ArrayList<>();
        private final CompletableFuture<Void> completion = new CompletableFuture<>();
        private long head, fence, snapshotHead;
        private boolean initialized, hadResume, fenceSeen, snapshotSeen, snapshotDone, bootstrapScheduled, recovering, stopped;
        Stream(Dataset dataset, List<? extends Block<?>> blocks, StreamSink sink) {
            this.dataset = dataset; this.blocks = blocks; this.sink = sink;
            synchronized (dataset.client) {
                try {
                    Resume resume = sink.resume();
                    if (resume != null) { hadResume = true; head = resume.through(); initialized = resume.initialized(); gaps.addAll(resume.gaps()); }
                    Request live = dataset.streamSubscribe(blocks, this::live);
                    active.add(live);
                    live.completion().whenComplete((ignored, error) -> { if (!stopped) fail(error == null ? new IllegalStateException("Stream subscription ended") : error); });
                    if (!initialized) {
                        Request request = dataset.streamSnapshot(blocks, this::snapshot);
                        active.add(request);
                        request.completion().whenComplete((ignored, error) -> {
                            if (stopped) return;
                            if (error != null) { fail(error); return; }
                            try {
                                if (!snapshotSeen) throw new IllegalStateException("Snapshot ended without header");
                                snapshotDone = true; advance();
                            } catch (Throwable failure) { fail(failure); }
                        });
                    }
                } catch (Throwable failure) { fail(failure); }
            }
        }
        private void live(Response response) throws Exception {
            if (response.templateId() == FeedControlDecoder.TEMPLATE_ID) {
                response.decode(control); control.wrapDataset(text); checkDataset(text);
                long after = control.afterMessageId(), through = control.throughMessageId();
                if (!fenceSeen) {
                    if (control.kind() != 1) throw new IllegalArgumentException("Stream must begin with a fence");
                    fenceSeen = true; fence = through;
                    if (!hadResume) { head = through; sink.checkpoint(new Resume(head, gaps, initialized)); }
                    else if (Long.compareUnsigned(head, fence) < 0) open(new Gap(head, fence));
                } else if (control.kind() == 2) open(new Gap(after, through));
                else if (control.kind() == 3) {
                    if (Long.compareUnsigned(through, head) > 0) { head = through; sink.checkpoint(new Resume(head, gaps, initialized)); }
                } else throw new IllegalArgumentException("Unexpected Stream control");
                advance();
            } else {
                if (!fenceSeen) throw new IllegalArgumentException("Data before subscription fence");
                batch.wrap(response); checkDataset(batch.datasetBytes()); sink.write(batch);
            }
        }
        private void snapshot(Response response) throws Exception {
            if (!snapshotSeen) {
                response.decode(snapshot); snapshotHead = snapshot.throughMessageId(); snapshotSeen = true;
                var missing = snapshot.gaps();
                if (missing.count() < 0 || missing.count() > response.body().capacity() / 16) throw new IllegalArgumentException("Invalid snapshot gap group");
                while (missing.hasNext()) { missing.next(); open(new Gap(missing.afterMessageId(), missing.throughMessageId())); }
                snapshot.wrapDataset(text); checkDataset(text);
            } else {
                batch.wrap(response); checkDataset(batch.datasetBytes()); sink.write(batch);
            }
        }
        private void checkDataset(DirectBuffer actual) {
            if (actual.capacity() != dataset.dataset.length) throw new IllegalArgumentException("Dataset mismatch");
            for (int i = 0; i < actual.capacity(); i++) if (actual.getByte(i) != dataset.dataset[i]) throw new IllegalArgumentException("Dataset mismatch");
        }
        private void open(Gap gap) throws Exception {
            if (!gaps.contains(gap)) gaps.add(gap);
            if (Long.compareUnsigned(gap.through(), head) > 0) head = gap.through();
            sink.checkpoint(new Resume(head, gaps, initialized));
        }
        private void advance() throws Exception {
            if (stopped || !fenceSeen) return;
            if (!initialized && snapshotDone && !bootstrapScheduled) {
                bootstrapScheduled = true;
                if (Long.compareUnsigned(snapshotHead, fence) < 0) open(new Gap(snapshotHead, fence));
            }
            if (!recovering && !gaps.isEmpty()) {
                recovering = true;
                Gap gap = gaps.get(0);
                Request recovery = dataset.streamRecover(gap.after(), gap.through(), blocks, response -> {
                    // An unexpected control must not silently turn a missing interval into coverage.
                    batch.wrap(response); checkDataset(batch.datasetBytes()); sink.write(batch);
                });
                active.add(recovery);
                recovery.completion().whenComplete((ignored, error) -> {
                    active.remove(recovery);
                    if (stopped) return;
                    if (error != null) { fail(error); return; }
                    try {
                        gaps.remove(gap); recovering = false;
                        sink.checkpoint(new Resume(head, gaps, initialized)); advance();
                    } catch (Throwable failure) { fail(failure); }
                });
            } else if (!initialized && snapshotDone && gaps.isEmpty()) {
                initialized = true;
                sink.checkpoint(new Resume(head, gaps, true));
            }
        }
        private void fail(Throwable error) {
            if (stopped) return;
            stopped = true; completion.completeExceptionally(error);
            for (Request request : List.copyOf(active)) request.close();
            active.clear();
        }
        public CompletableFuture<Void> completion() { return completion; }
        @Override public void close() {
            synchronized (dataset.client) {
                if (stopped) return;
                stopped = true;
                for (Request request : List.copyOf(active)) request.close();
                active.clear(); completion.cancel(false);
            }
        }
    }
    public static final class Catalog implements AutoCloseable {
        private final Request request;
        Catalog(Dataset dataset, List<? extends CatalogField<?>> fields, CatalogSink sink) {
            synchronized (dataset.client) {
                try {
                    sink.reset();
                    request = dataset.catalogSubscribe(fields, sink.resume(), new CatalogListener() {
                        @Override public void onRecord(CatalogRecord record) throws Exception { sink.write(record); }
                        @Override public void onSnapshotBegin() throws Exception { sink.snapshotBegin(); }
                        @Override public void onSnapshotComplete(String cursor) throws Exception { sink.snapshotComplete(cursor); }
                        @Override public void onCursor(String cursor) throws Exception { sink.cursor(cursor); }
                    });
                } catch (Exception error) { throw new IllegalStateException("Cannot start Catalog sink", error); }
            }
        }
        public CompletableFuture<Void> completion() { return request.completion(); }
        @Override public void close() { request.close(); }
    }
    /** Allocating process-local convenience sink. Use a custom sink for a bounded durable store. */
    public static final class MemoryStream implements StreamSink, AutoCloseable {
        public record Value(long messageId, long eventTimeMicros, Format format, boolean clear, byte[] payload) {}
        public final Map<String, Map<Integer, Value>> records = new LinkedHashMap<>();
        private Resume state;
        public Stream handle;
        @Override public Resume resume() { return state; }
        @Override public void write(BatchView batch) {
            Map<Integer, Value> values = records.computeIfAbsent(batch.recordKey(), ignored -> new LinkedHashMap<>());
            batch.forEachField((id, field) -> {
                int key = (field.schemaId() << 16) | field.templateId();
                Value previous = values.get(key);
                if (previous != null && Long.compareUnsigned(previous.messageId(), id) >= 0) return;
                byte[] bytes = new byte[field.payload().capacity()]; field.payload().getBytes(0, bytes);
                values.put(key, new Value(id, field.eventTimeMicros(), new Format(field.schemaId(), field.templateId(), field.version(), field.blockLength()), field.clear(), bytes));
            });
        }
        @Override public void checkpoint(Resume resume) { state = resume; }
        @Override public void close() { if (handle != null) handle.close(); }
    }
    /** Allocating whole-record store with atomic staging-map replacement. */
    public static final class MemoryCatalog implements CatalogSink, AutoCloseable {
        public Map<String, Response.Owned> records = new LinkedHashMap<>();
        private Map<String, Response.Owned> staging;
        private String cursor;
        public Catalog handle;
        @Override public String resume() { return cursor; }
        @Override public void reset() { staging = null; }
        @Override public void snapshotBegin() { staging = new LinkedHashMap<>(); }
        @Override public void write(CatalogRecord record) {
            String id = record.recordKey();
            Map<String, Response.Owned> target = staging == null ? records : staging;
            if (!record.exists()) target.remove(id); else target.put(id, record.copy());
        }
        @Override public void snapshotComplete(String value) {
            if (staging == null) throw new IllegalStateException("No staged snapshot");
            records = staging; staging = null; cursor = value;
        }
        @Override public void cursor(String value) { cursor = value; }
        @Override public void close() { if (handle != null) handle.close(); }
    }
}
