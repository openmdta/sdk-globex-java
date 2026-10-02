package com.openmdta.sdk.p_globex;

import java.util.List;
import java.util.Objects;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.*;

/** Internal protocol adapter shared by the public Stream commands and sink workflows. */
final class StreamDispatch implements Request.Listener {
    enum Mode { SUBSCRIBE, RECOVER, SNAPSHOT }
    private final Mode mode;
    private final byte[] dataset;
    private final long after, through;
    private final StreamListener listener;
    private final MarketDataListener updates;
    private final MarketDataUpdate update;
    private final FeedControlDecoder control = new FeedControlDecoder();
    private final FeedSnapshotHeaderDecoder snapshot = new FeedSnapshotHeaderDecoder();
    private final UnsafeBuffer text = new UnsafeBuffer(0, 0);
    private boolean started;
    private Request request;

    StreamDispatch(Mode mode, byte[] dataset, long after, long through, List<? extends Block<?>> blocks, StreamListener listener) {
        this.mode = mode; this.dataset = dataset; this.after = after; this.through = through;
        this.listener = Objects.requireNonNull(listener);
        update = new MarketDataUpdate(blocks);
        updates = listener::onUpdate;
    }
    @Override public void onRegistered(Request request) { this.request = request; update.request = request; }
    @Override public void onReplay() throws Exception { started = false; listener.onReplay(); }
    @Override public void onResponse(Response response) throws Exception {
        if (response.templateId() == MarketDataMessageBatchDecoder.TEMPLATE_ID) {
            if (mode != Mode.RECOVER && !started) throw new IllegalArgumentException("Data before Stream boundary");
            update.wrap(response);
            checkDataset(update.datasetBytes());
            if (update.snapshot() != (mode == Mode.SNAPSHOT)) throw new IllegalArgumentException("Unexpected Stream data phase");
            if (update.batch.gaps().count() != 0) throw new IllegalArgumentException("Unexpected event-time gaps in source Stream");
            if (mode == Mode.RECOVER) update.batch.requireRange(after, through);
            update.deliver(updates);
        } else if (mode == Mode.SUBSCRIBE && response.templateId() == FeedControlDecoder.TEMPLATE_ID) {
            response.decode(control); control.wrapDataset(text); checkDataset(text);
            if (control.limit() != response.body().capacity()) throw new IllegalArgumentException("Invalid Stream control length");
            long start = control.afterMessageId(), end = control.throughMessageId();
            if (control.kind() == 1) {
                if (started || start != end) throw new IllegalArgumentException("Invalid subscription fence");
                started = true; listener.onFence(end);
            } else {
                if (!started || Long.compareUnsigned(start, end) > 0) throw new IllegalArgumentException("Invalid Stream coverage");
                switch (control.kind()) {
                    case 2 -> {
                        if (start == end) throw new IllegalArgumentException("Empty Stream gap");
                        listener.onGap(start, end);
                    }
                    case 3 -> listener.onWatermark(start, end);
                    default -> throw new IllegalArgumentException("Unknown Stream control");
                }
            }
        } else if (mode == Mode.SNAPSHOT && response.templateId() == FeedSnapshotHeaderDecoder.TEMPLATE_ID) {
            if (started) throw new IllegalArgumentException("Duplicate snapshot header");
            response.decode(snapshot);
            var gaps = snapshot.gaps();
            if (gaps.count() < 0 || gaps.actingBlockLength() < 16 || gaps.count() > response.body().capacity() / gaps.actingBlockLength()) {
                throw new IllegalArgumentException("Invalid snapshot gap group");
            }
            while (gaps.hasNext()) {
                gaps.next();
                if (Long.compareUnsigned(gaps.afterMessageId(), gaps.throughMessageId()) >= 0) throw new IllegalArgumentException("Invalid snapshot gap");
            }
            snapshot.wrapDataset(text); checkDataset(text);
            if (snapshot.limit() != response.body().capacity()) throw new IllegalArgumentException("Invalid snapshot header length");
            started = true; listener.onSnapshotBegin(snapshot.throughMessageId());
            snapshot.sbeRewind(); gaps = snapshot.gaps();
            while (gaps.hasNext()) {
                if (request != null && (request.closed || request.client.closed)) return;
                gaps.next(); listener.onSnapshotGap(gaps.afterMessageId(), gaps.throughMessageId());
            }
        } else throw new IllegalArgumentException("Unexpected Stream response");
    }
    @Override public void onComplete() throws Exception {
        if (mode != Mode.RECOVER && !started) throw new IllegalStateException("Stream ended without its initial boundary");
        if (mode == Mode.SNAPSHOT) listener.onSnapshotComplete();
    }
    private void checkDataset(DirectBuffer actual) {
        if (actual.capacity() != dataset.length) throw new IllegalArgumentException("Dataset mismatch");
        for (int i = 0; i < actual.capacity(); i++) if (actual.getByte(i) != dataset[i]) throw new IllegalArgumentException("Dataset mismatch");
    }
}
