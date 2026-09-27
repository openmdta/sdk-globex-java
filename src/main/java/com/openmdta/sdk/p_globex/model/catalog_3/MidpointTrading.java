package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record MidpointTrading(String midpoint_trading, String midpoint_execution_venue_id) {
    public static MidpointTrading decode(com.openmdta.sdk.p_globex.sbe.catalog_3.MidpointTradingDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.midpoint_trading();
        String result1 = decoder.midpoint_execution_venue_id();
        return new MidpointTrading(result0, result1);
    }
}

