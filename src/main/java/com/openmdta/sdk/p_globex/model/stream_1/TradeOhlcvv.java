package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record TradeOhlcvv(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close, long totalQuantity, java.math.BigDecimal totalTradedValue, long day, long flags) {
    public static TradeOhlcvv decode(com.openmdta.sdk.p_globex.sbe.stream_1.TradeOhlcvvDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.close().mantissa(), -decoder.close().exponent());
        long result4 = decoder.totalQuantity();
        java.math.BigDecimal result5 = java.math.BigDecimal.valueOf(decoder.totalTradedValue().mantissa(), -decoder.totalTradedValue().exponent());
        long result6 = decoder.day();
        long result7 = decoder.flags();
        return new TradeOhlcvv(result0, result1, result2, result3, result4, result5, result6, result7);
    }
}

