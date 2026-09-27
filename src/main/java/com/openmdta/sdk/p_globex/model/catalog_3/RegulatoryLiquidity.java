package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record RegulatoryLiquidity(String regulatory_liquid_instrument, String pre_trade_lis_value, String liquidity_class) {
    public static RegulatoryLiquidity decode(com.openmdta.sdk.p_globex.sbe.catalog_3.RegulatoryLiquidityDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.regulatory_liquid_instrument();
        String result1 = decoder.pre_trade_lis_value();
        String result2 = decoder.liquidity_class();
        return new RegulatoryLiquidity(result0, result1, result2);
    }
}

