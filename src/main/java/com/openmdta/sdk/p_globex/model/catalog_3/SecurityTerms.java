package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record SecurityTerms(String issue_date, String maturity_date, String country_of_issue) {
    public static SecurityTerms decode(com.openmdta.sdk.p_globex.sbe.catalog_3.SecurityTermsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.issue_date();
        String result1 = decoder.maturity_date();
        String result2 = decoder.country_of_issue();
        return new SecurityTerms(result0, result1, result2);
    }
}

