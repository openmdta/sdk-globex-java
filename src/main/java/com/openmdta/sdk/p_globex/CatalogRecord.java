package com.openmdta.sdk.p_globex;

import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.CatalogRecordDecoder;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.CatalogFeedControlDecoder;

/** Borrowed whole-record replacement, or deletion when exists() is false. */
public final class CatalogRecord {
    public enum Listing { NOT_LISTED, LISTED }
    public enum Activity { UNKNOWN, ACTIVE, INACTIVE }
    @FunctionalInterface public interface ValueConsumer<D> { void accept(DirectBuffer subfield, D value); }
    private final CatalogRecordDecoder record = new CatalogRecordDecoder();
    private final UnsafeBuffer catalog = new UnsafeBuffer(0, 0), identifier = new UnsafeBuffer(0, 0), key = new UnsafeBuffer(0, 0);
    private final IdentityHashMap<CatalogField<?>, Selected<?>> selected = new IdentityHashMap<>();
    private final HashMap<String, Selected<?>> selectedBySemantic = new HashMap<>();
    Response response;

    private CatalogRecord(List<? extends CatalogField<?>> fields) {
        for (CatalogField<?> field : fields.isEmpty() ? Fields.ALL : fields) {
            if (selected.put(field, new Selected<>(field)) != null) throw new IllegalArgumentException("Duplicate Catalog field");
            if (selectedBySemantic.put(field.semantic(), selected.get(field)) != null) throw new IllegalArgumentException("Duplicate Catalog semantic field");
        }
    }
    static Request.Listener listener(List<? extends CatalogField<?>> fields, byte[] dataset, boolean subscription, CatalogListener listener) {
        Objects.requireNonNull(listener);
        CatalogRecord view = new CatalogRecord(fields);
        var control = new CatalogFeedControlDecoder();
        var cursorBytes = new UnsafeBuffer(0, 0);
        return new Request.Listener() {
            private boolean staging;
            @Override public void onReplay() throws Exception { staging = false; listener.onReplay(); }
            @Override public void onResponse(Response response) throws Exception {
                if (response.templateId() == CatalogRecordDecoder.TEMPLATE_ID) {
                    view.wrap(response);
                    if (subscription && view.snapshot() != staging) throw new IllegalArgumentException("Unexpected Catalog record phase");
                    if (view.catalog.capacity() != dataset.length) throw new IllegalArgumentException("Unexpected Catalog dataset");
                    for (int i = 0; i < dataset.length; i++) if (view.catalog.getByte(i) != dataset[i]) throw new IllegalArgumentException("Unexpected Catalog dataset");
                    listener.onRecord(view);
                    return;
                }
                if (!subscription) throw new IllegalArgumentException("Unexpected Catalog read response");
                response.decode(control);
                int kind = control.kind();
                control.wrapCursor(cursorBytes);
                String cursor = cursorBytes.getStringWithoutLengthUtf8(0, cursorBytes.capacity());
                if (control.limit() != response.body().capacity()) throw new IllegalArgumentException("Invalid Catalog control length");
                switch (kind) {
                    case 1 -> {
                        if (staging || !cursor.isEmpty()) throw new IllegalStateException("Invalid Catalog snapshot begin");
                        staging = true; listener.onSnapshotBegin();
                    }
                    case 2, 3 -> {
                        boolean complete = kind == 2;
                        if (complete != staging || cursor.isEmpty()) throw new IllegalArgumentException("Unexpected Catalog cursor control");
                        if (complete) { listener.onSnapshotComplete(cursor); staging = false; }
                        else listener.onCursor(cursor);
                    }
                    default -> throw new IllegalArgumentException("Unknown Catalog control");
                }
            }
        };
    }
    private CatalogRecord wrap(Response response) {
        this.response = response;
        response.decode(record);
        if (record.exists() > 1 || record.lifecyclePresent() > 1 || record.visible() > 1
                || record.lifecyclePresent() == 1 && (record.listing() > 1 || record.activity() > 2
                || record.listing() == 0 && record.activity() != 0)) throw new IllegalArgumentException("Invalid Catalog lifecycle");
        int phase = record.phase().value();
        if (phase != 1 && phase != 2) throw new IllegalArgumentException("Unexpected Catalog record phase");
        var entries = record.fields();
        if (entries.count() < 0 || entries.actingBlockLength() < 6 || entries.count() > response.body().capacity() / entries.actingBlockLength()) {
            throw new IllegalArgumentException("Invalid Catalog field group");
        }
        while (entries.hasNext()) { entries.next(); entries.skipLabel(); entries.skipSubfield(); entries.skipPayload(); }
        record.wrapCatalog(catalog); record.wrapIdentifier(identifier); record.wrapRecordIdentifier(key);
        if (record.limit() != response.body().capacity()) throw new IllegalArgumentException("Invalid Catalog record");
        return this;
    }
    /** Generated decoder for a single-valued field, or null if absent/deleted. */
    public <D extends MessageDecoderFlyweight> D value(CatalogField<D> field) {
        Selected<D> selection = selected(field);
        selection.single = null;
        forEachValue(field, selection.readSingle);
        return selection.single;
    }
    private <D extends MessageDecoderFlyweight> Optional<D> optionalValue(CatalogField<D> field) {
        Selected<D> selection = selected(field);
        return value(field) == null ? selection.absent : selection.present;
    }
    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder> getDisplayName() { return optionalValue(Fields.GLOBEX__DISPLAY_NAME); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_0.DisplayName> displayName() {
        var decoder = value(Fields.GLOBEX__DISPLAY_NAME);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_0.DisplayName.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachDisplayName(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder> consumer) { forEachValue(Fields.GLOBEX__DISPLAY_NAME, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachDisplayNameValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_0.DisplayName> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.GLOBEX__DISPLAY_NAME, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_0.DisplayName.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_1.LusDomainObjectDecoder> getLusDomainObject() { return optionalValue(Fields.LUS__LUS_DOMAIN_OBJECT); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_1.LusDomainObject> lusDomainObject() {
        var decoder = value(Fields.LUS__LUS_DOMAIN_OBJECT);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_1.LusDomainObject.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachLusDomainObject(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_1.LusDomainObjectDecoder> consumer) { forEachValue(Fields.LUS__LUS_DOMAIN_OBJECT, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachLusDomainObjectValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_1.LusDomainObject> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.LUS__LUS_DOMAIN_OBJECT, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_1.LusDomainObject.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_1.LusNameHashDecoder> getLusNameHash() { return optionalValue(Fields.LUS__LUS_NAME_HASH); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_1.LusNameHash> lusNameHash() {
        var decoder = value(Fields.LUS__LUS_NAME_HASH);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_1.LusNameHash.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachLusNameHash(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_1.LusNameHashDecoder> consumer) { forEachValue(Fields.LUS__LUS_NAME_HASH, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachLusNameHashValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_1.LusNameHash> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.LUS__LUS_NAME_HASH, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_1.LusNameHash.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskDecoder> getBidAsk() { return optionalValue(Fields.OPENMDTA__BID_ASK); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BidAsk> bidAsk() {
        var decoder = value(Fields.OPENMDTA__BID_ASK);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BidAsk.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBidAsk(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskDecoder> consumer) { forEachValue(Fields.OPENMDTA__BID_ASK, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBidAskValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BidAsk> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BID_ASK, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BidAsk.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder> getTrade() { return optionalValue(Fields.OPENMDTA__TRADE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.Trade> trade() {
        var decoder = value(Fields.OPENMDTA__TRADE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.Trade.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTrade(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.Trade> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.Trade.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleDecoder> getBidAskCandle() { return optionalValue(Fields.OPENMDTA__BID_ASK_CANDLE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandle> bidAskCandle() {
        var decoder = value(Fields.OPENMDTA__BID_ASK_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandle.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBidAskCandle(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleDecoder> consumer) { forEachValue(Fields.OPENMDTA__BID_ASK_CANDLE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBidAskCandleValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandle> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BID_ASK_CANDLE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandle.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleV2Decoder> getBidAskCandleV2() { return optionalValue(Fields.OPENMDTA__BID_ASK_CANDLE_V2); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandleV2> bidAskCandleV2() {
        var decoder = value(Fields.OPENMDTA__BID_ASK_CANDLE_V2);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandleV2.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBidAskCandleV2(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleV2Decoder> consumer) { forEachValue(Fields.OPENMDTA__BID_ASK_CANDLE_V2, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBidAskCandleV2Value(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandleV2> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BID_ASK_CANDLE_V2, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BidAskCandleV2.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleDecoder> getTradeCandle() { return optionalValue(Fields.OPENMDTA__TRADE_CANDLE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeCandle> tradeCandle() {
        var decoder = value(Fields.OPENMDTA__TRADE_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeCandle.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeCandle(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleDecoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_CANDLE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeCandleValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeCandle> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_CANDLE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeCandle.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleV2Decoder> getTradeCandleV2() { return optionalValue(Fields.OPENMDTA__TRADE_CANDLE_V2); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeCandleV2> tradeCandleV2() {
        var decoder = value(Fields.OPENMDTA__TRADE_CANDLE_V2);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeCandleV2.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeCandleV2(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleV2Decoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_CANDLE_V2, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeCandleV2Value(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeCandleV2> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_CANDLE_V2, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeCandleV2.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleDecoder> getTradeOhlcvvCandle() { return optionalValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandle> tradeOhlcvvCandle() {
        var decoder = value(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandle.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeOhlcvvCandle(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleDecoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeOhlcvvCandleValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandle> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandle.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleV2Decoder> getTradeOhlcvvCandleV2() { return optionalValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE_V2); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandleV2> tradeOhlcvvCandleV2() {
        var decoder = value(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE_V2);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandleV2.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeOhlcvvCandleV2(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleV2Decoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE_V2, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeOhlcvvCandleV2Value(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandleV2> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_OHLCVV_CANDLE_V2, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvvCandleV2.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BidDailyOhlcDecoder> getBidDailyOhlc() { return optionalValue(Fields.OPENMDTA__BID_DAILY_OHLC); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BidDailyOhlc> bidDailyOhlc() {
        var decoder = value(Fields.OPENMDTA__BID_DAILY_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BidDailyOhlc.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBidDailyOhlc(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BidDailyOhlcDecoder> consumer) { forEachValue(Fields.OPENMDTA__BID_DAILY_OHLC, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBidDailyOhlcValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BidDailyOhlc> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BID_DAILY_OHLC, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BidDailyOhlc.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.AskDailyOhlcDecoder> getAskDailyOhlc() { return optionalValue(Fields.OPENMDTA__ASK_DAILY_OHLC); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.AskDailyOhlc> askDailyOhlc() {
        var decoder = value(Fields.OPENMDTA__ASK_DAILY_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.AskDailyOhlc.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachAskDailyOhlc(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.AskDailyOhlcDecoder> consumer) { forEachValue(Fields.OPENMDTA__ASK_DAILY_OHLC, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachAskDailyOhlcValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.AskDailyOhlc> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__ASK_DAILY_OHLC, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.AskDailyOhlc.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDailyOhlcDecoder> getTradeDailyOhlc() { return optionalValue(Fields.OPENMDTA__TRADE_DAILY_OHLC); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeDailyOhlc> tradeDailyOhlc() {
        var decoder = value(Fields.OPENMDTA__TRADE_DAILY_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeDailyOhlc.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeDailyOhlc(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDailyOhlcDecoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_DAILY_OHLC, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeDailyOhlcValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeDailyOhlc> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_DAILY_OHLC, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeDailyOhlc.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BidOhlcDecoder> getBidOhlc() { return optionalValue(Fields.OPENMDTA__BID_OHLC); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BidOhlc> bidOhlc() {
        var decoder = value(Fields.OPENMDTA__BID_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BidOhlc.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBidOhlc(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BidOhlcDecoder> consumer) { forEachValue(Fields.OPENMDTA__BID_OHLC, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBidOhlcValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BidOhlc> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BID_OHLC, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BidOhlc.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.AskOhlcDecoder> getAskOhlc() { return optionalValue(Fields.OPENMDTA__ASK_OHLC); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.AskOhlc> askOhlc() {
        var decoder = value(Fields.OPENMDTA__ASK_OHLC);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.AskOhlc.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachAskOhlc(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.AskOhlcDecoder> consumer) { forEachValue(Fields.OPENMDTA__ASK_OHLC, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachAskOhlcValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.AskOhlc> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__ASK_OHLC, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.AskOhlc.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvDecoder> getTradeOhlcvv() { return optionalValue(Fields.OPENMDTA__TRADE_OHLCVV); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvv> tradeOhlcvv() {
        var decoder = value(Fields.OPENMDTA__TRADE_OHLCVV);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvv.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradeOhlcvv(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvDecoder> consumer) { forEachValue(Fields.OPENMDTA__TRADE_OHLCVV, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradeOhlcvvValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvv> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__TRADE_OHLCVV, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.TradeOhlcvv.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentDefinitionDecoder> getInstrumentDefinition() { return optionalValue(Fields.OPENMDTA__INSTRUMENT_DEFINITION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentDefinition> instrumentDefinition() {
        var decoder = value(Fields.OPENMDTA__INSTRUMENT_DEFINITION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.InstrumentDefinition.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentDefinition(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentDefinitionDecoder> consumer) { forEachValue(Fields.OPENMDTA__INSTRUMENT_DEFINITION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentDefinitionValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentDefinition> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__INSTRUMENT_DEFINITION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.InstrumentDefinition.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingDecoder> getListing() { return optionalValue(Fields.OPENMDTA__LISTING); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.Listing> listingValue() {
        var decoder = value(Fields.OPENMDTA__LISTING);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.Listing.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListing(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.Listing> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.Listing.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyProfileDecoder> getCompanyProfile() { return optionalValue(Fields.OPENMDTA__COMPANY_PROFILE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.CompanyProfile> companyProfile() {
        var decoder = value(Fields.OPENMDTA__COMPANY_PROFILE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.CompanyProfile.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachCompanyProfile(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyProfileDecoder> consumer) { forEachValue(Fields.OPENMDTA__COMPANY_PROFILE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachCompanyProfileValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.CompanyProfile> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__COMPANY_PROFILE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.CompanyProfile.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyAddressDecoder> getCompanyAddress() { return optionalValue(Fields.OPENMDTA__COMPANY_ADDRESS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.CompanyAddress> companyAddress() {
        var decoder = value(Fields.OPENMDTA__COMPANY_ADDRESS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.CompanyAddress.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachCompanyAddress(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyAddressDecoder> consumer) { forEachValue(Fields.OPENMDTA__COMPANY_ADDRESS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachCompanyAddressValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.CompanyAddress> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__COMPANY_ADDRESS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.CompanyAddress.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentIssuerDecoder> getInstrumentIssuer() { return optionalValue(Fields.OPENMDTA__INSTRUMENT_ISSUER); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentIssuer> instrumentIssuer() {
        var decoder = value(Fields.OPENMDTA__INSTRUMENT_ISSUER);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.InstrumentIssuer.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentIssuer(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentIssuerDecoder> consumer) { forEachValue(Fields.OPENMDTA__INSTRUMENT_ISSUER, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentIssuerValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentIssuer> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__INSTRUMENT_ISSUER, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.InstrumentIssuer.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.BasicMasterdataDecoder> getBasicMasterdata() { return optionalValue(Fields.OPENMDTA__BASIC_MASTERDATA); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.BasicMasterdata> basicMasterdata() {
        var decoder = value(Fields.OPENMDTA__BASIC_MASTERDATA);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.BasicMasterdata.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachBasicMasterdata(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.BasicMasterdataDecoder> consumer) { forEachValue(Fields.OPENMDTA__BASIC_MASTERDATA, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachBasicMasterdataValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.BasicMasterdata> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__BASIC_MASTERDATA, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.BasicMasterdata.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> getInstrumentNames() { return optionalValue(Fields.OPENMDTA__INSTRUMENT_NAMES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentNames> instrumentNames() {
        var decoder = value(Fields.OPENMDTA__INSTRUMENT_NAMES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.InstrumentNames.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentNames(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> consumer) { forEachValue(Fields.OPENMDTA__INSTRUMENT_NAMES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentNamesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentNames> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__INSTRUMENT_NAMES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.InstrumentNames.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.LegalEntityNamesDecoder> getLegalEntityNames() { return optionalValue(Fields.OPENMDTA__LEGAL_ENTITY_NAMES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.LegalEntityNames> legalEntityNames() {
        var decoder = value(Fields.OPENMDTA__LEGAL_ENTITY_NAMES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.LegalEntityNames.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachLegalEntityNames(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.LegalEntityNamesDecoder> consumer) { forEachValue(Fields.OPENMDTA__LEGAL_ENTITY_NAMES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachLegalEntityNamesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.LegalEntityNames> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LEGAL_ENTITY_NAMES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.LegalEntityNames.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> getClassification() { return optionalValue(Fields.OPENMDTA__CLASSIFICATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.Classification> classification() {
        var decoder = value(Fields.OPENMDTA__CLASSIFICATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.Classification.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachClassification(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> consumer) { forEachValue(Fields.OPENMDTA__CLASSIFICATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachClassificationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.Classification> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__CLASSIFICATION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.Classification.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder> getListingClassification() { return optionalValue(Fields.OPENMDTA__LISTING_CLASSIFICATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingClassification> listingClassification() {
        var decoder = value(Fields.OPENMDTA__LISTING_CLASSIFICATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingClassification.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingClassification(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_CLASSIFICATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingClassificationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingClassification> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_CLASSIFICATION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingClassification.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder> getListingQuotation() { return optionalValue(Fields.OPENMDTA__LISTING_QUOTATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingQuotation> listingQuotation() {
        var decoder = value(Fields.OPENMDTA__LISTING_QUOTATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingQuotation.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingQuotation(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_QUOTATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingQuotationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingQuotation> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_QUOTATION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingQuotation.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder> getListingVenue() { return optionalValue(Fields.OPENMDTA__LISTING_VENUE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingVenue> listingVenue() {
        var decoder = value(Fields.OPENMDTA__LISTING_VENUE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingVenue.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingVenue(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_VENUE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingVenueValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingVenue> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_VENUE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingVenue.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder> getListingTradingDates() { return optionalValue(Fields.OPENMDTA__LISTING_TRADING_DATES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingDates> listingTradingDates() {
        var decoder = value(Fields.OPENMDTA__LISTING_TRADING_DATES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingDates.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingTradingDates(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_TRADING_DATES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingTradingDatesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingDates> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_TRADING_DATES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingDates.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder> getListingTradingRules() { return optionalValue(Fields.OPENMDTA__LISTING_TRADING_RULES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingRules> listingTradingRules() {
        var decoder = value(Fields.OPENMDTA__LISTING_TRADING_RULES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingRules.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingTradingRules(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_TRADING_RULES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingTradingRulesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingRules> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_TRADING_RULES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingTradingRules.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder> getListingQuoteParameters() { return optionalValue(Fields.OPENMDTA__LISTING_QUOTE_PARAMETERS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingQuoteParameters> listingQuoteParameters() {
        var decoder = value(Fields.OPENMDTA__LISTING_QUOTE_PARAMETERS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingQuoteParameters.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingQuoteParameters(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_QUOTE_PARAMETERS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingQuoteParametersValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingQuoteParameters> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_QUOTE_PARAMETERS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingQuoteParameters.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder> getListingOrderSize() { return optionalValue(Fields.OPENMDTA__LISTING_ORDER_SIZE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingOrderSize> listingOrderSize() {
        var decoder = value(Fields.OPENMDTA__LISTING_ORDER_SIZE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingOrderSize.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingOrderSize(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_ORDER_SIZE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingOrderSizeValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingOrderSize> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_ORDER_SIZE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingOrderSize.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder> getListingTickSchedule() { return optionalValue(Fields.OPENMDTA__LISTING_TICK_SCHEDULE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListingTickSchedule> listingTickSchedule() {
        var decoder = value(Fields.OPENMDTA__LISTING_TICK_SCHEDULE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListingTickSchedule.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListingTickSchedule(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder> consumer) { forEachValue(Fields.OPENMDTA__LISTING_TICK_SCHEDULE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListingTickScheduleValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListingTickSchedule> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LISTING_TICK_SCHEDULE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListingTickSchedule.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentPrimaryListingDecoder> getInstrumentPrimaryListing() { return optionalValue(Fields.OPENMDTA__INSTRUMENT_PRIMARY_LISTING); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentPrimaryListing> instrumentPrimaryListing() {
        var decoder = value(Fields.OPENMDTA__INSTRUMENT_PRIMARY_LISTING);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.InstrumentPrimaryListing.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentPrimaryListing(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentPrimaryListingDecoder> consumer) { forEachValue(Fields.OPENMDTA__INSTRUMENT_PRIMARY_LISTING, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentPrimaryListingValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentPrimaryListing> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__INSTRUMENT_PRIMARY_LISTING, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.InstrumentPrimaryListing.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentListingRecordCountDecoder> getInstrumentListingRecordCount() { return optionalValue(Fields.OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentListingRecordCount> instrumentListingRecordCount() {
        var decoder = value(Fields.OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.InstrumentListingRecordCount.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentListingRecordCount(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentListingRecordCountDecoder> consumer) { forEachValue(Fields.OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentListingRecordCountValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.InstrumentListingRecordCount> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.InstrumentListingRecordCount.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateActionsDecoder> getCorporateActions() { return optionalValue(Fields.OPENMDTA__CORPORATE_ACTIONS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.CorporateActions> corporateActions() {
        var decoder = value(Fields.OPENMDTA__CORPORATE_ACTIONS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.CorporateActions.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachCorporateActions(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateActionsDecoder> consumer) { forEachValue(Fields.OPENMDTA__CORPORATE_ACTIONS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachCorporateActionsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.CorporateActions> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__CORPORATE_ACTIONS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.CorporateActions.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder> getDistributions() { return optionalValue(Fields.OPENMDTA__DISTRIBUTIONS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.Distributions> distributions() {
        var decoder = value(Fields.OPENMDTA__DISTRIBUTIONS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.Distributions.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachDistributions(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder> consumer) { forEachValue(Fields.OPENMDTA__DISTRIBUTIONS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachDistributionsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.Distributions> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__DISTRIBUTIONS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.Distributions.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ListDefinitionDecoder> getListDefinition() { return optionalValue(Fields.OPENMDTA__LIST_DEFINITION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.ListDefinition> listDefinition() {
        var decoder = value(Fields.OPENMDTA__LIST_DEFINITION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.ListDefinition.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachListDefinition(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ListDefinitionDecoder> consumer) { forEachValue(Fields.OPENMDTA__LIST_DEFINITION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachListDefinitionValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.ListDefinition> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.OPENMDTA__LIST_DEFINITION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_2.ListDefinition.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.SimDomainObjectDecoder> getSimDomainObject() { return optionalValue(Fields.SIM__SIM_DOMAIN_OBJECT); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.SimDomainObject> simDomainObject() {
        var decoder = value(Fields.SIM__SIM_DOMAIN_OBJECT);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.SimDomainObject.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachSimDomainObject(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.SimDomainObjectDecoder> consumer) { forEachValue(Fields.SIM__SIM_DOMAIN_OBJECT, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachSimDomainObjectValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.SimDomainObject> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.SIM__SIM_DOMAIN_OBJECT, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.SimDomainObject.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_4.XetraListingDecoder> getXetraListing() { return optionalValue(Fields.XETRA__XETRA_LISTING); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_4.XetraListing> xetraListing() {
        var decoder = value(Fields.XETRA__XETRA_LISTING);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_4.XetraListing.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraListing(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_4.XetraListingDecoder> consumer) { forEachValue(Fields.XETRA__XETRA_LISTING, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraListingValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_4.XetraListing> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__XETRA_LISTING, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_4.XetraListing.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_4.XetraMarketDetailsDecoder> getXetraMarketDetails() { return optionalValue(Fields.XETRA__XETRA_MARKET_DETAILS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_4.XetraMarketDetails> xetraMarketDetails() {
        var decoder = value(Fields.XETRA__XETRA_MARKET_DETAILS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_4.XetraMarketDetails.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraMarketDetails(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_4.XetraMarketDetailsDecoder> consumer) { forEachValue(Fields.XETRA__XETRA_MARKET_DETAILS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraMarketDetailsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_4.XetraMarketDetails> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__XETRA_MARKET_DETAILS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_4.XetraMarketDetails.decode(decoder)));
    }
    /** Handles repeated/subfield values without allocating a map or a decoder for each entry. */
    public <D extends MessageDecoderFlyweight> void forEachValue(CatalogField<D> field, ValueConsumer<D> consumer) {
        Objects.requireNonNull(consumer);
        Selected<D> selection = selected(field);
        if (!exists()) return;
        response.decode(selection.scan);
        var entries = selection.scan.fields();
        while (entries.hasNext()) {
            entries.next(); entries.wrapLabel(selection.label); entries.wrapSubfield(selection.subfield); entries.wrapPayload(selection.body);
            if (selection.label.capacity() != selection.name.length) continue;
            boolean matches = true;
            for (int i = 0; i < selection.name.length; i++) if (selection.label.getByte(i) != selection.name[i]) { matches = false; break; }
            if (matches) {
                selection.payload.wrap(selection.body);
                field.wrap(selection.decoder, selection.payload);
                consumer.accept(selection.subfield, selection.decoder);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private <D extends MessageDecoderFlyweight> Selected<D> selected(CatalogField<D> field) {
        Selected<?> value = selected.get(field);
        if (value == null) value = selectedBySemantic.get(field.semantic());
        if (value == null) throw new IllegalArgumentException("Catalog field was not selected by this request");
        return (Selected<D>) value; // Identity lookup pairs the descriptor with its own decoder supplier.
    }
    private static final class Selected<D extends MessageDecoderFlyweight> {
        final byte[] name;
        final D decoder;
        final Optional<D> present, absent = Optional.empty();
        final CatalogRecordDecoder scan = new CatalogRecordDecoder();
        final UnsafeBuffer label = new UnsafeBuffer(0, 0), subfield = new UnsafeBuffer(0, 0), body = new UnsafeBuffer(0, 0), payload = new UnsafeBuffer(0, 0);
        final ValueConsumer<D> readSingle;
        D single;
        Selected(CatalogField<D> field) {
            name = field.label().getBytes(StandardCharsets.UTF_8); decoder = field.decoder().get();
            present = Optional.of(decoder);
            readSingle = (key, value) -> {
                if (key.capacity() != 0 || single != null) throw new IllegalStateException("Use forEachValue for a repeated Catalog field");
                single = value;
            };
        }
    }
    public boolean exists() { return record.exists() == 1; }
    public boolean snapshot() { return record.phase().value() == 1; }
    public boolean hasLifecycle() { return record.lifecyclePresent() == 1; }
    public Listing listing() { return !hasLifecycle() ? null : record.listing() == 1 ? Listing.LISTED : Listing.NOT_LISTED; }
    public Activity activity() { return listing() != Listing.LISTED ? null : switch (record.activity()) { case 1 -> Activity.ACTIVE; case 2 -> Activity.INACTIVE; default -> Activity.UNKNOWN; }; }
    public boolean visible() { return hasLifecycle() && record.visible() == 1; }
    public long effectiveTimeMicros() { return record.effectiveTimeMicros(); }
    /** -1 is the unsigned uint64 null sentinel. */
    public long hideAtMicros() { return record.hideAtMicros(); }
    /** -1 is the unsigned uint64 null sentinel. */
    public long hiddenAtUnixSeconds() { return record.hiddenAtUnixSeconds(); }
    public DirectBuffer recordKeyBytes() { return key; }
    public DirectBuffer datasetBytes() { return catalog; }
    public DirectBuffer identifierBytes() { return identifier; }
    /** Allocating convenience accessor. */
    public String recordKey() { return key.getStringWithoutLengthUtf8(0, key.capacity()); }
    /** Allocating independent typed copy. Decoders are still mutable and not thread-safe. */
    public CatalogRecord copy() {
        CatalogRecord copy = new CatalogRecord(List.copyOf(selected.keySet()));
        return copy.wrap(response.copy().view());
    }
}
