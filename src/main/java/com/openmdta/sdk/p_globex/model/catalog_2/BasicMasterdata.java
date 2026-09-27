package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record BasicMasterdata(String name) {
    public static BasicMasterdata decode(com.openmdta.sdk.p_globex.sbe.catalog_2.BasicMasterdataDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.name();
        return new BasicMasterdata(result0);
    }
}

