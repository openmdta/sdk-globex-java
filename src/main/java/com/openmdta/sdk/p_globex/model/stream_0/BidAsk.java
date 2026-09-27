package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record BidAsk(java.util.Optional<QuoteLevel> bid, java.util.Optional<QuoteLevel> ask, java.util.OptionalInt quoteCondition) {
    public static BidAsk decode(com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.util.Optional<QuoteLevel> result0 = (decoder.bid().price().mantissa() == com.openmdta.sdk.p_globex.sbe.stream_0.DecimalEncodingDecoder.mantissaNullValue() && decoder.bid().price().exponent() == com.openmdta.sdk.p_globex.sbe.stream_0.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteLevel.decode(decoder.bid(), actingVersion));
        java.util.Optional<QuoteLevel> result1 = (decoder.ask().price().mantissa() == com.openmdta.sdk.p_globex.sbe.stream_0.DecimalEncodingDecoder.mantissaNullValue() && decoder.ask().price().exponent() == com.openmdta.sdk.p_globex.sbe.stream_0.DecimalEncodingDecoder.exponentNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(QuoteLevel.decode(decoder.ask(), actingVersion));
        java.util.OptionalInt result2 = (decoder.quoteCondition() == com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder.quoteConditionNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.quoteCondition());
        return new BidAsk(result0, result1, result2);
    }
}

