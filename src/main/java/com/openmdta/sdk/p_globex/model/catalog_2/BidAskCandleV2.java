package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record BidAskCandleV2(java.util.Optional<QuoteSideCandleV2> bid, java.util.Optional<QuoteSideCandleV2> ask, java.util.OptionalLong quoteCount, java.util.OptionalInt closingQuoteCondition) {
    public static BidAskCandleV2 decode(com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleV2Decoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.util.Optional<QuoteSideCandleV2> result0 = (decoder.bid().open().mantissa() == com.openmdta.sdk.p_globex.sbe.catalog_2.DecimalEncodingDecoder.mantissaNullValue() && decoder.bid().open().exponent() == com.openmdta.sdk.p_globex.sbe.catalog_2.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteSideCandleV2.decode(decoder.bid(), actingVersion));
        java.util.Optional<QuoteSideCandleV2> result1 = (decoder.ask().open().mantissa() == com.openmdta.sdk.p_globex.sbe.catalog_2.DecimalEncodingDecoder.mantissaNullValue() && decoder.ask().open().exponent() == com.openmdta.sdk.p_globex.sbe.catalog_2.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteSideCandleV2.decode(decoder.ask(), actingVersion));
        java.util.OptionalLong result2 = (decoder.quoteCount() == com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleV2Decoder.quoteCountNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.quoteCount());
        java.util.OptionalInt result3 = (decoder.closingQuoteCondition() == com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleV2Decoder.closingQuoteConditionNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.closingQuoteCondition());
        return new BidAskCandleV2(result0, result1, result2, result3);
    }
}

