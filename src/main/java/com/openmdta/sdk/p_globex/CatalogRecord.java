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
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> getOpenmdtaClassification() { return optionalValue(Fields.OPENMDTA__CLASSIFICATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_2.Classification> openmdtaClassification() {
        var decoder = value(Fields.OPENMDTA__CLASSIFICATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_2.Classification.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachOpenmdtaClassification(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> consumer) { forEachValue(Fields.OPENMDTA__CLASSIFICATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachOpenmdtaClassificationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_2.Classification> consumer) {
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
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraRecordDecoder> getXetraRecord() { return optionalValue(Fields.XETRA__XETRA_RECORD); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.XetraRecord> xetraRecord() {
        var decoder = value(Fields.XETRA__XETRA_RECORD);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.XetraRecord.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraRecord(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraRecordDecoder> consumer) { forEachValue(Fields.XETRA__XETRA_RECORD, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraRecordValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.XetraRecord> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__XETRA_RECORD, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.XetraRecord.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.IdentifiersDecoder> getIdentifiers() { return optionalValue(Fields.XETRA__IDENTIFIERS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Identifiers> identifiers() {
        var decoder = value(Fields.XETRA__IDENTIFIERS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Identifiers.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachIdentifiers(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.IdentifiersDecoder> consumer) { forEachValue(Fields.XETRA__IDENTIFIERS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachIdentifiersValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Identifiers> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__IDENTIFIERS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Identifiers.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.DisplayNamesDecoder> getDisplayNames() { return optionalValue(Fields.XETRA__DISPLAY_NAMES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.DisplayNames> displayNames() {
        var decoder = value(Fields.XETRA__DISPLAY_NAMES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.DisplayNames.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachDisplayNames(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.DisplayNamesDecoder> consumer) { forEachValue(Fields.XETRA__DISPLAY_NAMES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachDisplayNamesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.DisplayNames> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__DISPLAY_NAMES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.DisplayNames.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder> getXetraClassification() { return optionalValue(Fields.XETRA__CLASSIFICATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Classification> xetraClassification() {
        var decoder = value(Fields.XETRA__CLASSIFICATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Classification.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraClassification(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder> consumer) { forEachValue(Fields.XETRA__CLASSIFICATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraClassificationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Classification> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__CLASSIFICATION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Classification.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.LifecycleDecoder> getLifecycle() { return optionalValue(Fields.XETRA__LIFECYCLE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Lifecycle> lifecycle() {
        var decoder = value(Fields.XETRA__LIFECYCLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Lifecycle.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachLifecycle(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.LifecycleDecoder> consumer) { forEachValue(Fields.XETRA__LIFECYCLE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachLifecycleValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Lifecycle> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__LIFECYCLE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Lifecycle.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingVenueDecoder> getTradingVenue() { return optionalValue(Fields.XETRA__TRADING_VENUE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.TradingVenue> tradingVenue() {
        var decoder = value(Fields.XETRA__TRADING_VENUE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.TradingVenue.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradingVenue(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingVenueDecoder> consumer) { forEachValue(Fields.XETRA__TRADING_VENUE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradingVenueValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.TradingVenue> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__TRADING_VENUE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.TradingVenue.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingDatesDecoder> getTradingDates() { return optionalValue(Fields.XETRA__TRADING_DATES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.TradingDates> tradingDates() {
        var decoder = value(Fields.XETRA__TRADING_DATES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.TradingDates.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradingDates(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingDatesDecoder> consumer) { forEachValue(Fields.XETRA__TRADING_DATES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradingDatesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.TradingDates> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__TRADING_DATES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.TradingDates.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.QuotationDecoder> getQuotation() { return optionalValue(Fields.XETRA__QUOTATION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Quotation> quotation() {
        var decoder = value(Fields.XETRA__QUOTATION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Quotation.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachQuotation(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.QuotationDecoder> consumer) { forEachValue(Fields.XETRA__QUOTATION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachQuotationValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Quotation> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__QUOTATION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Quotation.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingRulesDecoder> getTradingRules() { return optionalValue(Fields.XETRA__TRADING_RULES); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.TradingRules> tradingRules() {
        var decoder = value(Fields.XETRA__TRADING_RULES);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.TradingRules.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTradingRules(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingRulesDecoder> consumer) { forEachValue(Fields.XETRA__TRADING_RULES, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTradingRulesValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.TradingRules> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__TRADING_RULES, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.TradingRules.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.MidpointTradingDecoder> getMidpointTrading() { return optionalValue(Fields.XETRA__MIDPOINT_TRADING); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.MidpointTrading> midpointTrading() {
        var decoder = value(Fields.XETRA__MIDPOINT_TRADING);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.MidpointTrading.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachMidpointTrading(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.MidpointTradingDecoder> consumer) { forEachValue(Fields.XETRA__MIDPOINT_TRADING, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachMidpointTradingValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.MidpointTrading> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__MIDPOINT_TRADING, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.MidpointTrading.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.SettlementDecoder> getSettlement() { return optionalValue(Fields.XETRA__SETTLEMENT); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Settlement> settlement() {
        var decoder = value(Fields.XETRA__SETTLEMENT);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Settlement.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachSettlement(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.SettlementDecoder> consumer) { forEachValue(Fields.XETRA__SETTLEMENT, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachSettlementValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Settlement> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__SETTLEMENT, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Settlement.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteParametersDecoder> getQuoteParameters() { return optionalValue(Fields.XETRA__QUOTE_PARAMETERS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.QuoteParameters> quoteParameters() {
        var decoder = value(Fields.XETRA__QUOTE_PARAMETERS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.QuoteParameters.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachQuoteParameters(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteParametersDecoder> consumer) { forEachValue(Fields.XETRA__QUOTE_PARAMETERS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachQuoteParametersValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.QuoteParameters> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__QUOTE_PARAMETERS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.QuoteParameters.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderSizeDecoder> getOrderSize() { return optionalValue(Fields.XETRA__ORDER_SIZE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.OrderSize> orderSize() {
        var decoder = value(Fields.XETRA__ORDER_SIZE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.OrderSize.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachOrderSize(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderSizeDecoder> consumer) { forEachValue(Fields.XETRA__ORDER_SIZE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachOrderSizeValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.OrderSize> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__ORDER_SIZE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.OrderSize.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderRiskLimitsDecoder> getOrderRiskLimits() { return optionalValue(Fields.XETRA__ORDER_RISK_LIMITS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.OrderRiskLimits> orderRiskLimits() {
        var decoder = value(Fields.XETRA__ORDER_RISK_LIMITS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.OrderRiskLimits.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachOrderRiskLimits(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderRiskLimitsDecoder> consumer) { forEachValue(Fields.XETRA__ORDER_RISK_LIMITS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachOrderRiskLimitsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.OrderRiskLimits> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__ORDER_RISK_LIMITS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.OrderRiskLimits.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.IcebergRequirementsDecoder> getIcebergRequirements() { return optionalValue(Fields.XETRA__ICEBERG_REQUIREMENTS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.IcebergRequirements> icebergRequirements() {
        var decoder = value(Fields.XETRA__ICEBERG_REQUIREMENTS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.IcebergRequirements.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachIcebergRequirements(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.IcebergRequirementsDecoder> consumer) { forEachValue(Fields.XETRA__ICEBERG_REQUIREMENTS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachIcebergRequirementsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.IcebergRequirements> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__ICEBERG_REQUIREMENTS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.IcebergRequirements.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.TickSizeTableDecoder> getTickSizeTable() { return optionalValue(Fields.XETRA__TICK_SIZE_TABLE); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.TickSizeTable> tickSizeTable() {
        var decoder = value(Fields.XETRA__TICK_SIZE_TABLE);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.TickSizeTable.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachTickSizeTable(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.TickSizeTableDecoder> consumer) { forEachValue(Fields.XETRA__TICK_SIZE_TABLE, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachTickSizeTableValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.TickSizeTable> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__TICK_SIZE_TABLE, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.TickSizeTable.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.DesignatedSponsorsDecoder> getDesignatedSponsors() { return optionalValue(Fields.XETRA__DESIGNATED_SPONSORS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.DesignatedSponsors> designatedSponsors() {
        var decoder = value(Fields.XETRA__DESIGNATED_SPONSORS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.DesignatedSponsors.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachDesignatedSponsors(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.DesignatedSponsorsDecoder> consumer) { forEachValue(Fields.XETRA__DESIGNATED_SPONSORS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachDesignatedSponsorsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.DesignatedSponsors> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__DESIGNATED_SPONSORS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.DesignatedSponsors.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.MarketMakersDecoder> getMarketMakers() { return optionalValue(Fields.XETRA__MARKET_MAKERS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.MarketMakers> marketMakers() {
        var decoder = value(Fields.XETRA__MARKET_MAKERS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.MarketMakers.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachMarketMakers(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.MarketMakersDecoder> consumer) { forEachValue(Fields.XETRA__MARKET_MAKERS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachMarketMakersValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.MarketMakers> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__MARKET_MAKERS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.MarketMakers.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.SpecialistsDecoder> getSpecialists() { return optionalValue(Fields.XETRA__SPECIALISTS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Specialists> specialists() {
        var decoder = value(Fields.XETRA__SPECIALISTS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Specialists.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachSpecialists(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.SpecialistsDecoder> consumer) { forEachValue(Fields.XETRA__SPECIALISTS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachSpecialistsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Specialists> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__SPECIALISTS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Specialists.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.RegulatoryLiquidityDecoder> getRegulatoryLiquidity() { return optionalValue(Fields.XETRA__REGULATORY_LIQUIDITY); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.RegulatoryLiquidity> regulatoryLiquidity() {
        var decoder = value(Fields.XETRA__REGULATORY_LIQUIDITY);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.RegulatoryLiquidity.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachRegulatoryLiquidity(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.RegulatoryLiquidityDecoder> consumer) { forEachValue(Fields.XETRA__REGULATORY_LIQUIDITY, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachRegulatoryLiquidityValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.RegulatoryLiquidity> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__REGULATORY_LIQUIDITY, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.RegulatoryLiquidity.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityCorridorsDecoder> getVolatilityCorridors() { return optionalValue(Fields.XETRA__VOLATILITY_CORRIDORS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.VolatilityCorridors> volatilityCorridors() {
        var decoder = value(Fields.XETRA__VOLATILITY_CORRIDORS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.VolatilityCorridors.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachVolatilityCorridors(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityCorridorsDecoder> consumer) { forEachValue(Fields.XETRA__VOLATILITY_CORRIDORS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachVolatilityCorridorsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.VolatilityCorridors> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__VOLATILITY_CORRIDORS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.VolatilityCorridors.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityInterruptionLimitsDecoder> getVolatilityInterruptionLimits() { return optionalValue(Fields.XETRA__VOLATILITY_INTERRUPTION_LIMITS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.VolatilityInterruptionLimits> volatilityInterruptionLimits() {
        var decoder = value(Fields.XETRA__VOLATILITY_INTERRUPTION_LIMITS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.VolatilityInterruptionLimits.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachVolatilityInterruptionLimits(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityInterruptionLimitsDecoder> consumer) { forEachValue(Fields.XETRA__VOLATILITY_INTERRUPTION_LIMITS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachVolatilityInterruptionLimitsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.VolatilityInterruptionLimits> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__VOLATILITY_INTERRUPTION_LIMITS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.VolatilityInterruptionLimits.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.SecurityTermsDecoder> getSecurityTerms() { return optionalValue(Fields.XETRA__SECURITY_TERMS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.SecurityTerms> securityTerms() {
        var decoder = value(Fields.XETRA__SECURITY_TERMS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.SecurityTerms.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachSecurityTerms(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.SecurityTermsDecoder> consumer) { forEachValue(Fields.XETRA__SECURITY_TERMS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachSecurityTermsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.SecurityTerms> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__SECURITY_TERMS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.SecurityTerms.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.DerivativeTermsDecoder> getDerivativeTerms() { return optionalValue(Fields.XETRA__DERIVATIVE_TERMS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.DerivativeTerms> derivativeTerms() {
        var decoder = value(Fields.XETRA__DERIVATIVE_TERMS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.DerivativeTerms.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachDerivativeTerms(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.DerivativeTermsDecoder> consumer) { forEachValue(Fields.XETRA__DERIVATIVE_TERMS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachDerivativeTermsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.DerivativeTerms> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__DERIVATIVE_TERMS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.DerivativeTerms.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.CouponTermsDecoder> getCouponTerms() { return optionalValue(Fields.XETRA__COUPON_TERMS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.CouponTerms> couponTerms() {
        var decoder = value(Fields.XETRA__COUPON_TERMS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.CouponTerms.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachCouponTerms(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.CouponTermsDecoder> consumer) { forEachValue(Fields.XETRA__COUPON_TERMS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachCouponTermsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.CouponTerms> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__COUPON_TERMS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.CouponTerms.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionDecoder> getCorporateAction() { return optionalValue(Fields.XETRA__CORPORATE_ACTION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.CorporateAction> corporateAction() {
        var decoder = value(Fields.XETRA__CORPORATE_ACTION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.CorporateAction.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachCorporateAction(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionDecoder> consumer) { forEachValue(Fields.XETRA__CORPORATE_ACTION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachCorporateActionValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.CorporateAction> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__CORPORATE_ACTION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.CorporateAction.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.FeedPartitionDecoder> getFeedPartition() { return optionalValue(Fields.XETRA__FEED_PARTITION); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.FeedPartition> feedPartition() {
        var decoder = value(Fields.XETRA__FEED_PARTITION);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.FeedPartition.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachFeedPartition(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.FeedPartitionDecoder> consumer) { forEachValue(Fields.XETRA__FEED_PARTITION, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachFeedPartitionValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.FeedPartition> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__FEED_PARTITION, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.FeedPartition.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.EmdiDecoder> getEmdi() { return optionalValue(Fields.XETRA__EMDI); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Emdi> emdi() {
        var decoder = value(Fields.XETRA__EMDI);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Emdi.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachEmdi(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.EmdiDecoder> consumer) { forEachValue(Fields.XETRA__EMDI, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachEmdiValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Emdi> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__EMDI, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Emdi.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.MdiDecoder> getMdi() { return optionalValue(Fields.XETRA__MDI); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Mdi> mdi() {
        var decoder = value(Fields.XETRA__MDI);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Mdi.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachMdi(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.MdiDecoder> consumer) { forEachValue(Fields.XETRA__MDI, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachMdiValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Mdi> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__MDI, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Mdi.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.EobiDecoder> getEobi() { return optionalValue(Fields.XETRA__EOBI); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.Eobi> eobi() {
        var decoder = value(Fields.XETRA__EOBI);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.Eobi.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachEobi(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.EobiDecoder> consumer) { forEachValue(Fields.XETRA__EOBI, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachEobiValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.Eobi> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__EOBI, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.Eobi.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentInputDecoder> getInstrumentInput() { return optionalValue(Fields.XETRA__INSTRUMENT_INPUT); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.InstrumentInput> instrumentInput() {
        var decoder = value(Fields.XETRA__INSTRUMENT_INPUT);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.InstrumentInput.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachInstrumentInput(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentInputDecoder> consumer) { forEachValue(Fields.XETRA__INSTRUMENT_INPUT, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachInstrumentInputValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.InstrumentInput> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__INSTRUMENT_INPUT, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.InstrumentInput.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraListingDecoder> getXetraListing() { return optionalValue(Fields.XETRA__XETRA_LISTING); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.XetraListing> xetraListing() {
        var decoder = value(Fields.XETRA__XETRA_LISTING);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.XetraListing.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraListing(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraListingDecoder> consumer) { forEachValue(Fields.XETRA__XETRA_LISTING, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraListingValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.XetraListing> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__XETRA_LISTING, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.XetraListing.decode(decoder)));
    }

    /** Borrowed decoder, empty if absent/deleted. Requires selection; rejects repeated/subfield values. No allocation. */
    public java.util.Optional<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraMarketDetailsDecoder> getXetraMarketDetails() { return optionalValue(Fields.XETRA__XETRA_MARKET_DETAILS); }
    /** Allocates an immutable owned value; empty if absent/deleted. Requires single-value selection. */
    public java.util.Optional<com.openmdta.sdk.p_globex.model.catalog_3.XetraMarketDetails> xetraMarketDetails() {
        var decoder = value(Fields.XETRA__XETRA_MARKET_DETAILS);
        return decoder == null ? java.util.Optional.empty() : java.util.Optional.of(com.openmdta.sdk.p_globex.model.catalog_3.XetraMarketDetails.decode(decoder));
    }
    /** Visits borrowed subfield keys and decoders. Requires selection; no allocation. */
    public void forEachXetraMarketDetails(ValueConsumer<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraMarketDetailsDecoder> consumer) { forEachValue(Fields.XETRA__XETRA_MARKET_DETAILS, consumer); }
    /** Visits owned values with borrowed subfield keys. */
    public void forEachXetraMarketDetailsValue(ValueConsumer<com.openmdta.sdk.p_globex.model.catalog_3.XetraMarketDetails> consumer) {
        java.util.Objects.requireNonNull(consumer);
        forEachValue(Fields.XETRA__XETRA_MARKET_DETAILS, (key, decoder) -> consumer.accept(key, com.openmdta.sdk.p_globex.model.catalog_3.XetraMarketDetails.decode(decoder)));
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
