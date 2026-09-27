package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record InstrumentPrimaryListing(String catalog, String recordKey) {
    public static InstrumentPrimaryListing decode(com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentPrimaryListingDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.catalog();
        String result1 = decoder.recordKey();
        return new InstrumentPrimaryListing(result0, result1);
    }
}

