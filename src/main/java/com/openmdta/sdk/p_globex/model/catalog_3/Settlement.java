package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Settlement(String ccp_eligible_code, String clearing_location, String settlement_period, String settlement_currency, String multi_ccp_eligible, String deposit_type) {
    public static Settlement decode(com.openmdta.sdk.p_globex.sbe.catalog_3.SettlementDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.ccp_eligible_code();
        String result1 = decoder.clearing_location();
        String result2 = decoder.settlement_period();
        String result3 = decoder.settlement_currency();
        String result4 = decoder.multi_ccp_eligible();
        String result5 = decoder.deposit_type();
        return new Settlement(result0, result1, result2, result3, result4, result5);
    }
}

