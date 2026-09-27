package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record QuoteLevel(java.math.BigDecimal price, long size) {
    public static QuoteLevel decode(com.openmdta.sdk.p_globex.sbe.stream_0.QuoteLevelDecoder decoder, int actingVersion) {
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.price().mantissa(), -decoder.price().exponent());
        long result1 = decoder.size();
        return new QuoteLevel(result0, result1);
    }
}

