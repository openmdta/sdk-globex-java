package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record AskOhlc(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close, long closingSize, long day, long flags) {
    public static AskOhlc decode(com.openmdta.sdk.p_globex.sbe.catalog_2.AskOhlcDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.close().mantissa(), -decoder.close().exponent());
        long result4 = decoder.closingSize();
        long result5 = decoder.day();
        long result6 = decoder.flags();
        return new AskOhlc(result0, result1, result2, result3, result4, result5, result6);
    }
}

