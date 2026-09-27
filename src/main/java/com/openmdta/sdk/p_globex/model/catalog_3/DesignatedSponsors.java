package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record DesignatedSponsors(String designated_sponsor_member_id, String designated_sponsor) {
    public static DesignatedSponsors decode(com.openmdta.sdk.p_globex.sbe.catalog_3.DesignatedSponsorsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.designated_sponsor_member_id();
        String result1 = decoder.designated_sponsor();
        return new DesignatedSponsors(result0, result1);
    }
}

