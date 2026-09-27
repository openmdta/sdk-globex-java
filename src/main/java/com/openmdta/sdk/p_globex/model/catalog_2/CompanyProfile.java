package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record CompanyProfile(short hasLei, String canonicalName, String lei) {
    public static CompanyProfile decode(com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyProfileDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        short result0 = decoder.hasLei();
        String result1 = decoder.canonicalName();
        String result2 = decoder.lei();
        return new CompanyProfile(result0, result1, result2);
    }
}

