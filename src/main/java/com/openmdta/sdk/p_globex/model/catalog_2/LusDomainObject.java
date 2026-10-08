package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record LusDomainObject(String name) {
    public static LusDomainObject decode(com.openmdta.sdk.p_globex.sbe.catalog_2.LusDomainObjectDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.name();
        return new LusDomainObject(result0);
    }
}

