package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record LusNameHash(String value) {
    public static LusNameHash decode(com.openmdta.sdk.p_globex.sbe.catalog_2.LusNameHashDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.value();
        return new LusNameHash(result0);
    }
}

