package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Specialists(String specialist_member_id, String specialist, String liquidity_provider_user_group, String specialist_user_group) {
    public static Specialists decode(com.openmdta.sdk.p_globex.sbe.catalog_3.SpecialistsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.specialist_member_id();
        String result1 = decoder.specialist();
        String result2 = decoder.liquidity_provider_user_group();
        String result3 = decoder.specialist_user_group();
        return new Specialists(result0, result1, result2, result3);
    }
}

