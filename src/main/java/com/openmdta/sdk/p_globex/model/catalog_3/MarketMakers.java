package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record MarketMakers(String market_maker_member_id, String market_maker) {
    public static MarketMakers decode(com.openmdta.sdk.p_globex.sbe.catalog_3.MarketMakersDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.market_maker_member_id();
        String result1 = decoder.market_maker();
        return new MarketMakers(result0, result1);
    }
}

