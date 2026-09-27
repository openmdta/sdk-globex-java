package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Identifiers(String isin, String product_id, String instrument_id, String wkn) {
    public static Identifiers decode(com.openmdta.sdk.p_globex.sbe.catalog_3.IdentifiersDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.isin();
        String result1 = decoder.product_id();
        String result2 = decoder.instrument_id();
        String result3 = decoder.wkn();
        return new Identifiers(result0, result1, result2, result3);
    }
}

