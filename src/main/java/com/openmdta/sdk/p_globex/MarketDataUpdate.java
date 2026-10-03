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
    // Slots follow the selection order (at most 64); responses name blocks by owner schema and template ID.
    private final Block<?>[] blocks;
    private final MessageDecoderFlyweight[] values;
    private final Optional<?>[] present;
    private final UnsafeBuffer[] payloads;
    private final long[] eventTimes;
    private final int[] lengths, versions;
    private long changed, cleared, messageId;
    private static final int UNRESOLVED = -2;
    private int[] slotByField = new int[0];

    MarketDataUpdate(List<? extends Block<?>> selection) {
        this.selection = List.copyOf(selection);
        int count = this.selection.size();
        if (count > 64) throw new IllegalArgumentException("At most 64 blocks can be selected");
        blocks = new Block<?>[count]; values = new MessageDecoderFlyweight[count]; present = new Optional<?>[count];
        payloads = new UnsafeBuffer[count]; eventTimes = new long[count]; lengths = new int[count]; versions = new int[count];
        for (int slot = 0; slot < count; slot++) {
            Block<?> block = this.selection.get(slot);
            if (slot(block.semantic(), block.layout()) >= 0) throw new IllegalArgumentException("Duplicate block selection");
            blocks[slot] = block; values[slot] = block.decoder().get(); payloads[slot] = new UnsafeBuffer(0, 0);
            present[slot] = Optional.of(values[slot]);
        }
        fieldConsumer = field -> {
            int slot = slotOf(field);
            if (slot < 0) throw new IllegalArgumentException("Unexpected block in market-data response");
            long bit = 1L << slot;
            if ((changed & bit) != 0) throw new IllegalArgumentException("Duplicate block in source message");
            changed |= bit; eventTimes[slot] = field.eventTimeMicros();
            if (field.clear()) { cleared |= bit; return; }
            payloads[slot].wrap(field.payload());
            lengths[slot] = field.blockLength(); versions[slot] = field.version();
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
            @Override public void onReplay() throws Exception { listener.onReplay(); }
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
        for (int id = 0; id < blocks.length; id++) {
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
        return (Optional<D>) (decoder == null ? EMPTY : present[selected(block)]);
    }
    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.AskOhlcDecoder> getAskOhlc() { return optionalValue(Blocks.ASK_OHLC); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.AskOhlc> askOhlc() {
        var decoder = value(Blocks.ASK_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.AskOhlc.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isAskOhlcChanged() { return changed(Blocks.ASK_OHLC); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isAskOhlcCleared() { return cleared(Blocks.ASK_OHLC); }

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
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskCandleDecoder> getBidAskCandle() { return optionalValue(Blocks.BID_ASK_CANDLE); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.BidAskCandle> bidAskCandle() {
        var decoder = value(Blocks.BID_ASK_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.BidAskCandle.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isBidAskCandleChanged() { return changed(Blocks.BID_ASK_CANDLE); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isBidAskCandleCleared() { return cleared(Blocks.BID_ASK_CANDLE); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.BidOhlcDecoder> getBidOhlc() { return optionalValue(Blocks.BID_OHLC); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.BidOhlc> bidOhlc() {
        var decoder = value(Blocks.BID_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.BidOhlc.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isBidOhlcChanged() { return changed(Blocks.BID_OHLC); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isBidOhlcCleared() { return cleared(Blocks.BID_OHLC); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.TradeDecoder> getTrade() { return optionalValue(Blocks.TRADE); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.Trade> trade() {
        var decoder = value(Blocks.TRADE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.Trade.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isTradeChanged() { return changed(Blocks.TRADE); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isTradeCleared() { return cleared(Blocks.TRADE); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvDecoder> getTradeOhlcvv() { return optionalValue(Blocks.TRADE_OHLCVV); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.TradeOhlcvv> tradeOhlcvv() {
        var decoder = value(Blocks.TRADE_OHLCVV);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.TradeOhlcvv.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isTradeOhlcvvChanged() { return changed(Blocks.TRADE_OHLCVV); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isTradeOhlcvvCleared() { return cleared(Blocks.TRADE_OHLCVV); }

    /** Borrowed decoder, empty if unchanged or cleared. Selection is required; no allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvCandleDecoder> getTradeOhlcvvCandle() { return optionalValue(Blocks.TRADE_OHLCVV_CANDLE); }
    /** Allocates an immutable owned value with nested Optionals and exact BigDecimal prices. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.stream_0.TradeOhlcvvCandle> tradeOhlcvvCandle() {
        var decoder = value(Blocks.TRADE_OHLCVV_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.stream_0.TradeOhlcvvCandle.decode(decoder));
    }
    /** True for a value or an explicit clear in this source message. Selection is required. */
    public boolean isTradeOhlcvvCandleChanged() { return changed(Blocks.TRADE_OHLCVV_CANDLE); }
    /** True only for an explicit clear in this source message. Selection is required. */
    public boolean isTradeOhlcvvCandleCleared() { return cleared(Blocks.TRADE_OHLCVV_CANDLE); }
    public boolean changed(Block<?> block) { return (changed & (1L << selected(block))) != 0; }
    public boolean cleared(Block<?> block) { return (cleared & (1L << selected(block))) != 0; }
    public long eventTimeMicros(Block<?> block) {
        int id = selected(block);
        if ((changed & (1L << id)) == 0) throw new IllegalStateException("Block did not change in this message");
        return eventTimes[id];
    }
    private int selected(Block<?> block) {
        int slot = slot(block.semantic(), block.layout());
        if (slot < 0 || blocks[slot] != block) throw new IllegalArgumentException("Block was not selected by this request");
        return slot;
    }
    private int slot(String semantic, String layout) {
        for (int slot = 0; slot < blocks.length; slot++) {
            Block<?> block = blocks[slot];
            if (block != null && block.semantic().equals(semantic) && block.layout().equals(layout)) return slot;
        }
        return -1;
    }
    /** Slot of an announced field ID, resolved by name once per ID and cached; no allocation after that. */
    private int slotOf(BatchView.Field field) {
        int id = field.fieldId();
        if (id >= slotByField.length) {
            int[] grown = java.util.Arrays.copyOf(slotByField, Math.max(id + 1, slotByField.length * 2));
            java.util.Arrays.fill(grown, slotByField.length, grown.length, UNRESOLVED);
            slotByField = grown;
        }
        if (slotByField[id] == UNRESOLVED) slotByField[id] = slot(field.semantic(), field.layout());
        return slotByField[id];
    }
    public long messageId() { return messageId; }
    public boolean snapshot() { return snapshot; }
    public DirectBuffer recordKeyBytes() { return key; }
    public DirectBuffer datasetBytes() { return dataset; }
    /** Allocating convenience accessor. */
    public String recordKey() { return key.getStringWithoutLengthUtf8(0, key.capacity()); }
}
