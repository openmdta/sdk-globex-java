package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TradeOhlcvvCandle(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close, long totalVolume, long tradeCount, java.math.BigDecimal totalTradedValue) {
    public static TradeOhlcvvCandle decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TradeOhlcvvCandleDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.close().mantissa(), -decoder.close().exponent());
        long result4 = decoder.totalVolume();
        long result5 = decoder.tradeCount();
        java.math.BigDecimal result6 = java.math.BigDecimal.valueOf(decoder.totalTradedValue().mantissa(), -decoder.totalTradedValue().exponent());
        return new TradeOhlcvvCandle(result0, result1, result2, result3, result4, result5, result6);
    }
}

