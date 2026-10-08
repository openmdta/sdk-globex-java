package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record BidAskCandle(java.util.Optional<QuoteSideCandle> bid, java.util.Optional<QuoteSideCandle> ask, long quoteCount, java.util.OptionalInt closingQuoteCondition) {
    public static BidAskCandle decode(com.openmdta.sdk.p_globex.sbe.catalog_3.BidAskCandleDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.util.Optional<QuoteSideCandle> result0 = (decoder.bid().open().mantissa() == com.openmdta.sdk.p_globex.sbe.catalog_3.DecimalEncodingDecoder.mantissaNullValue() && decoder.bid().open().exponent() == com.openmdta.sdk.p_globex.sbe.catalog_3.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteSideCandle.decode(decoder.bid(), actingVersion));
        java.util.Optional<QuoteSideCandle> result1 = (decoder.ask().open().mantissa() == com.openmdta.sdk.p_globex.sbe.catalog_3.DecimalEncodingDecoder.mantissaNullValue() && decoder.ask().open().exponent() == com.openmdta.sdk.p_globex.sbe.catalog_3.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteSideCandle.decode(decoder.ask(), actingVersion));
        long result2 = decoder.quoteCount();
        java.util.OptionalInt result3 = (decoder.closingQuoteCondition() == com.openmdta.sdk.p_globex.sbe.catalog_3.BidAskCandleDecoder.closingQuoteConditionNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.closingQuoteCondition());
        return new BidAskCandle(result0, result1, result2, result3);
    }
}

