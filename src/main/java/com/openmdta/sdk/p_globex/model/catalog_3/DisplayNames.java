package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record DisplayNames(String instrument, String mnemonic) {
    public static DisplayNames decode(com.openmdta.sdk.p_globex.sbe.catalog_3.DisplayNamesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.instrument();
        String result1 = decoder.mnemonic();
        return new DisplayNames(result0, result1);
    }
}

