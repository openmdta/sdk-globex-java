package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record BasicMasterdata(String name) {
    public static BasicMasterdata decode(com.openmdta.sdk.p_globex.sbe.catalog_3.BasicMasterdataDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.name();
        return new BasicMasterdata(result0);
    }
}

