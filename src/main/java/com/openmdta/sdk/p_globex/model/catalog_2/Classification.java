package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record Classification(String code, String label) {
    public static Classification decode(com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.code();
        String result1 = decoder.label();
        return new Classification(result0, result1);
    }
}

