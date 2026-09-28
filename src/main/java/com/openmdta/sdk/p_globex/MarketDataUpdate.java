package com.openmdta.sdk.p_globex;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/** One source message, with typed, borrowed block decoders. Reused for the next message. */
public final class MarketDataUpdate {
    private static final Optional<?> EMPTY = Optional.empty();
    Request request;
    final BatchView batch = new BatchView();
    final List<Block<?>> selection;
    private final UnsafeBuffer key = new UnsafeBuffer(0, 0), dataset = new UnsafeBuffer(0, 0);
    private final BatchView.FieldConsumer fieldConsumer;
    private final BatchView.MessageConsumer messageConsumer;
    private MarketDataListener listener;
    private boolean snapshot;
    private final Block<?>[] blocks = new Block<?>[64];
    private final MessageDecoderFlyweight[] values = new MessageDecoderFlyweight[64];
    private final Optional<?>[] present = new Optional<?>[64];
    private final UnsafeBuffer[] payloads = new UnsafeBuffer[64];
    private final long[] eventTimes = new long[64];
    private final int[] lengths = new int[64], versions = new int[64];
    private long changed, cleared, messageId;

    MarketDataUpdate(List<? extends Block<?>> selection) {
        this.selection = List.copyOf(selection);
        for (Block<?> block : selection) {
            int id = block.format().templateId();
            if (id >= 64 || blocks[id] != null) throw new IllegalArgumentException("Invalid or duplicate block selection");
            blocks[id] = block; values[id] = block.decoder().get(); payloads[id] = new UnsafeBuffer(0, 0);
            present[id] = Optional.of(values[id]);
        }
        fieldConsumer = field -> {
            int template = field.templateId();
            if (template >= 64 || blocks[template] == null || blocks[template].format().schemaId() != field.schemaId()) {
                throw new IllegalArgumentException("Unexpected block in market-data response");
            }
            long bit = 1L << template;
            if ((changed & bit) != 0) throw new IllegalArgumentException("Duplicate block in source message");
            changed |= bit; eventTimes[template] = field.eventTimeMicros();
            if (field.clear()) { cleared |= bit; return; }
            payloads[template].wrap(field.payload());
            lengths[template] = field.blockLength(); versions[template] = field.version();
        };
        messageConsumer = message -> {
            if (request != null && (request.closed || request.client.closed)) return;
            changed = 0; cleared = 0;
            messageId = message.messageId(); snapshot = message.snapshot();
            key.wrap(message.recordKeyBytes()); dataset.wrap(message.datasetBytes());
            message.forEachField(fieldConsumer);
            listener.onUpdate(this);
        };
    }
    static Request.Listener listener(List<? extends Block<?>> blocks, MarketDataListener listener) {
        Objects.requireNonNull(listener);
        MarketDataUpdate update = new MarketDataUpdate(blocks);
        return new Request.Listener() {
            @Override public void onRegistered(Request request) { update.request = request; }
            @Override public void onResponse(Response response) throws Exception { update.wrap(response).deliver(listener); }
        };
    }
    MarketDataUpdate wrap(Response response) {
        batch.wrap(response);
        key.wrap(batch.recordKeyBytes()); dataset.wrap(batch.datasetBytes()); snapshot = batch.snapshot();
        return this;
    }
    void deliver(MarketDataListener listener) throws Exception {
        this.listener = listener;
        try {
            batch.forEachMessage(messageConsumer);
            var gaps = batch.gaps();
            while (gaps.hasNext()) {
                if (request != null && (request.closed || request.client.closed)) return;
                gaps.next(); listener.onGap(gaps.fromEventTimeMicros(), gaps.throughEventTimeMicros());
            }
        } finally {
            this.listener = null; changed = 0; cleared = 0;
        }
    }
    /** Allocating independent copy of this source message. Decoders are still mutable and not thread-safe. */
    public MarketDataUpdate copy() {
        MarketDataUpdate copy = new MarketDataUpdate(selection);
        byte[] keyBytes = new byte[key.capacity()], datasetBytes = new byte[dataset.capacity()];
        key.getBytes(0, keyBytes); dataset.getBytes(0, datasetBytes);
        copy.key.wrap(keyBytes); copy.dataset.wrap(datasetBytes);
        copy.snapshot = snapshot; copy.messageId = messageId; copy.changed = changed; copy.cleared = cleared;
        for (Block<?> block : selection) {
            int id = block.format().templateId();
            copy.eventTimes[id] = eventTimes[id]; copy.lengths[id] = lengths[id]; copy.versions[id] = versions[id];
            if ((changed & ~cleared & (1L << id)) == 0) continue;
            byte[] bytes = new byte[payloads[id].capacity()];
            payloads[id].getBytes(0, bytes); copy.payloads[id].wrap(bytes);
        }
        return copy;
    }
    /** Returns the generated decoder, or null for an unchanged/cleared block. Never allocates. */
    @SuppressWarnings("unchecked")
    public <D extends MessageDecoderFlyweight> D value(Block<D> block) {
        int id = selected(block);
        if ((changed & ~cleared & (1L << id)) == 0) return null;
        values[id].wrap(payloads[id], 0, lengths[id], versions[id]);
        return (D) values[id]; // Each slot was constructed by this exact typed descriptor.
    }
    @SuppressWarnings("unchecked")
    private <D extends MessageDecoderFlyweight> Optional<D> optionalValue(Block<D> block) {
        D decoder = value(block); // Validate selection and rewind the borrowed decoder.
        return (Optional<D>) (decoder == null ? EMPTY : present[block.format().templateId()]);
    }
    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.AskDailyOhlcDecoder> getAskDailyOhlc() { return optionalValue(Blocks.ASK_DAILY_OHLC); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.AskDailyOhlc> askDailyOhlc() {
        var decoder = value(Blocks.ASK_DAILY_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.AskDailyOhlc.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isAskDailyOhlcChanged() { return changed(Blocks.ASK_DAILY_OHLC); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isAskDailyOhlcCleared() { return cleared(Blocks.ASK_DAILY_OHLC); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder> getBidAsk() { return optionalValue(Blocks.BID_ASK); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.BidAsk> bidAsk() {
        var decoder = value(Blocks.BID_ASK);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.BidAsk.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isBidAskChanged() { return changed(Blocks.BID_ASK); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isBidAskCleared() { return cleared(Blocks.BID_ASK); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_1.BidAskCandleDecoder> getBidAskCandle() { return optionalValue(Blocks.BID_ASK_CANDLE); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_1.BidAskCandle> bidAskCandle() {
        var decoder = value(Blocks.BID_ASK_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_1.BidAskCandle.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isBidAskCandleChanged() { return changed(Blocks.BID_ASK_CANDLE); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isBidAskCandleCleared() { return cleared(Blocks.BID_ASK_CANDLE); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.BidDailyOhlcDecoder> getBidDailyOhlc() { return optionalValue(Blocks.BID_DAILY_OHLC); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.BidDailyOhlc> bidDailyOhlc() {
        var decoder = value(Blocks.BID_DAILY_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.BidDailyOhlc.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isBidDailyOhlcChanged() { return changed(Blocks.BID_DAILY_OHLC); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isBidDailyOhlcCleared() { return cleared(Blocks.BID_DAILY_OHLC); }
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
    public boolean snapshot() { return snapshot; }
    public DirectBuffer recordKeyBytes() { return key; }
    public DirectBuffer datasetBytes() { return dataset; }
    /** Allocating convenience accessor. */
    public String recordKey() { return key.getStringWithoutLengthUtf8(0, key.capacity()); }
}
