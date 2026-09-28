package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record TradeDailyOhlc(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal closeLast, java.math.BigDecimal closePrevious, long day, long flags) {
    public static TradeDailyOhlc decode(com.openmdta.sdk.p_globex.sbe.stream_1.TradeDailyOhlcDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.closeLast().mantissa(), -decoder.closeLast().exponent());
        java.math.BigDecimal result4 = java.math.BigDecimal.valueOf(decoder.closePrevious().mantissa(), -decoder.closePrevious().exponent());
        long result5 = decoder.day();
        long result6 = decoder.flags();
        return new TradeDailyOhlc(result0, result1, result2, result3, result4, result5, result6);
    }
}

