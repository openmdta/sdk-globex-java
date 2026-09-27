package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record Trade(java.math.BigDecimal price, long volume, java.util.OptionalInt saleConditionFlags) {
    public static Trade decode(com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.price().mantissa(), -decoder.price().exponent());
        long result1 = decoder.volume();
        java.util.OptionalInt result2 = (decoder.saleConditionFlags() == com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder.saleConditionFlagsNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.saleConditionFlags());
        return new Trade(result0, result1, result2);
    }
}

