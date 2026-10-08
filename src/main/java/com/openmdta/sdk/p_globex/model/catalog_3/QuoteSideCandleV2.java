package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record QuoteSideCandleV2(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close, java.util.OptionalLong closingSize) {
    public static QuoteSideCandleV2 decode(com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteSideCandleV2Decoder decoder, int actingVersion) {
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.open().mantissa(), -decoder.open().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.high().mantissa(), -decoder.high().exponent());
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.low().mantissa(), -decoder.low().exponent());
        java.math.BigDecimal result3 = java.math.BigDecimal.valueOf(decoder.close().mantissa(), -decoder.close().exponent());
        java.util.OptionalLong result4 = (decoder.closingSize() == com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteSideCandleV2Decoder.closingSizeNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.closingSize());
        return new QuoteSideCandleV2(result0, result1, result2, result3, result4);
    }
}

