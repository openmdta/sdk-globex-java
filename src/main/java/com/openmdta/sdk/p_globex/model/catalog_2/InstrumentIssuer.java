package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record InstrumentIssuer(String lei) {
    public static InstrumentIssuer decode(com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentIssuerDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.lei();
        return new InstrumentIssuer(result0);
    }
}

