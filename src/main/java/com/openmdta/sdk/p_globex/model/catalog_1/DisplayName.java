package com.openmdta.sdk.p_globex.model.catalog_1;

/** Immutable owned value; decoding allocates. */
public record DisplayName(String value) {
    public static DisplayName decode(com.openmdta.sdk.p_globex.sbe.catalog_1.DisplayNameDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.value();
        return new DisplayName(result0);
    }
}

