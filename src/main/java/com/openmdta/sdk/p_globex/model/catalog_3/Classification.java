package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Classification(String code, String label) {
    public static Classification decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.code();
        String result1 = decoder.label();
        return new Classification(result0, result1);
    }
}

