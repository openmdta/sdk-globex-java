package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record VolatilityCorridors(String volatility_corridor_opening_auction, String volatility_corridor_intraday_auction, String volatility_corridor_closing_auction, String volatility_corridor_continuous, String volatility_corridor_retail_auction) {
    public static VolatilityCorridors decode(com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityCorridorsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.volatility_corridor_opening_auction();
        String result1 = decoder.volatility_corridor_intraday_auction();
        String result2 = decoder.volatility_corridor_closing_auction();
        String result3 = decoder.volatility_corridor_continuous();
        String result4 = decoder.volatility_corridor_retail_auction();
        return new VolatilityCorridors(result0, result1, result2, result3, result4);
    }
}

