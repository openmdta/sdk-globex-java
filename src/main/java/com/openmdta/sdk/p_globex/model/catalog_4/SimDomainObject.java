package com.openmdta.sdk.p_globex.model.catalog_4;

/** Immutable owned value; decoding allocates. */
public record SimDomainObject(String name) {
    public static SimDomainObject decode(com.openmdta.sdk.p_globex.sbe.catalog_4.SimDomainObjectDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.name();
        return new SimDomainObject(result0);
    }
}

