package com.openmdta.sdk.p_globex.model.catalog_0;

/** Immutable owned value; decoding allocates. */
public record DisplayName(String value) {
    public static DisplayName decode(com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.value();
        return new DisplayName(result0);
    }
}

