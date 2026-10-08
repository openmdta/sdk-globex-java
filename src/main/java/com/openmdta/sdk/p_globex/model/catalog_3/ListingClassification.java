package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListingClassification(String code, String label) {
    public static ListingClassification decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListingClassificationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.code();
        String result1 = decoder.label();
        return new ListingClassification(result0, result1);
    }
}

