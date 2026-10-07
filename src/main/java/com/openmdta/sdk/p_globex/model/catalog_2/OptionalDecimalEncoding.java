package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record OptionalDecimalEncoding(long mantissa, byte exponent) {
    public static OptionalDecimalEncoding decode(com.openmdta.sdk.p_globex.sbe.catalog_2.OptionalDecimalEncodingDecoder decoder, int actingVersion) {
        long result0 = decoder.mantissa();
        byte result1 = decoder.exponent();
        return new OptionalDecimalEncoding(result0, result1);
    }
}

