package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TradeOhlcvvCandleV2(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close, long totalVolume, java.util.OptionalLong tradeCount, java.util.Optional<OptionalDecimalEncoding> totalTradedValue) {
    public static TradeOhlcvvCandleV2 decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TradeOhlcvvCandleV2Decoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.close().mantissa(), -decoder.close().exponent());
        long result4 = decoder.totalVolume();
        java.util.OptionalLong result5 = (decoder.tradeCount() == com.openmdta.sdk.p_globex.sbe.catalog_3.TradeOhlcvvCandleV2Decoder.tradeCountNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.tradeCount());
        java.util.Optional<OptionalDecimalEncoding> result6 = (decoder.totalTradedValue().mantissa() == com.openmdta.sdk.p_globex.sbe.catalog_3.OptionalDecimalEncodingDecoder.mantissaNullValue()) ? java.util.Optional.empty() : java.util.Optional.of(OptionalDecimalEncoding.decode(decoder.totalTradedValue(), actingVersion));
        return new TradeOhlcvvCandleV2(result0, result1, result2, result3, result4, result5, result6);
    }
}

