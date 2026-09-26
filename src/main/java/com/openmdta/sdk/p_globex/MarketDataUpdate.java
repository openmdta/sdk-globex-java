package com.openmdta.sdk.p_globex;

import java.util.List;
import java.util.Objects;
import java.util.function.LongConsumer;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/** One source message, with typed, borrowed block decoders. Reused for the next message. */
public final class MarketDataUpdate {
    private final BatchView batch = new BatchView();
    private final Block<?>[] blocks = new Block<?>[64];
    private final MessageDecoderFlyweight[] values = new MessageDecoderFlyweight[64];
    private final UnsafeBuffer[] payloads = new UnsafeBuffer[64];
    private final long[] eventTimes = new long[64];
    private final int[] lengths = new int[64], versions = new int[64];
    private long changed, cleared, messageId;

    private MarketDataUpdate(List<? extends Block<?>> selection) {
        for (Block<?> block : selection) {
            int id = block.format().templateId();
            if (id >= 64 || blocks[id] != null) throw new IllegalArgumentException("Invalid or duplicate block selection");
            blocks[id] = block; values[id] = block.decoder().get(); payloads[id] = new UnsafeBuffer(0, 0);
        }
    }
    static Request.Listener listener(List<? extends Block<?>> blocks, MarketDataListener listener) {
        Objects.requireNonNull(listener);
        MarketDataUpdate update = new MarketDataUpdate(blocks);
        BatchView.FieldConsumer fieldConsumer = (messageId, field) -> {
            int id = field.templateId();
            if (id >= 64 || update.blocks[id] == null || update.blocks[id].format().schemaId() != field.schemaId()) {
                throw new IllegalArgumentException("Unexpected block in market-data response");
            }
            long bit = 1L << id;
            if ((update.changed & bit) != 0) throw new IllegalArgumentException("Duplicate block in source message");
            update.changed |= bit; update.eventTimes[id] = field.eventTimeMicros();
            if (field.clear()) { update.cleared |= bit; return; }
            update.payloads[id].wrap(field.payload());
            update.lengths[id] = field.blockLength(); update.versions[id] = field.version();
            update.values[id].wrap(update.payloads[id], 0, field.blockLength(), field.version());
        };
        LongConsumer messageComplete = id -> {
            update.messageId = id;
            listener.onUpdate(update);
            update.changed = 0; update.cleared = 0;
        };
        return response -> {
            update.batch.wrap(response).forEachField(fieldConsumer, messageComplete);
            var gaps = update.batch.gaps();
            while (gaps.hasNext()) { gaps.next(); listener.onGap(gaps.fromEventTimeMicros(), gaps.throughEventTimeMicros()); }
        };
    }
    /** Returns the generated decoder, or null for an unchanged/cleared block. Never allocates. */
    @SuppressWarnings("unchecked")
    public <D extends MessageDecoderFlyweight> D value(Block<D> block) {
        int id = selected(block);
        if ((changed & ~cleared & (1L << id)) == 0) return null;
        values[id].wrap(payloads[id], 0, lengths[id], versions[id]);
        return (D) values[id]; // Each slot was constructed by this exact typed descriptor.
    }
    public boolean changed(Block<?> block) { return (changed & (1L << selected(block))) != 0; }
    public boolean cleared(Block<?> block) { return (cleared & (1L << selected(block))) != 0; }
    public long eventTimeMicros(Block<?> block) {
        int id = selected(block);
        if ((changed & (1L << id)) == 0) throw new IllegalStateException("Block did not change in this message");
        return eventTimes[id];
    }
    private int selected(Block<?> block) {
        int id = block.format().templateId();
        if (id >= 64 || blocks[id] != block) throw new IllegalArgumentException("Block was not selected by this request");
        return id;
    }
    public long messageId() { return messageId; }
    public boolean snapshot() { return batch.snapshot(); }
    public DirectBuffer recordKeyBytes() { return batch.recordKeyBytes(); }
    public DirectBuffer datasetBytes() { return batch.datasetBytes(); }
    /** Allocating convenience accessor. */
    public String recordKey() { return batch.recordKey(); }
}
