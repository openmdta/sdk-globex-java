package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record OptionalDecimalEncoding(long mantissa, byte exponent) {
    public static OptionalDecimalEncoding decode(com.openmdta.sdk.p_globex.sbe.stream_0.OptionalDecimalEncodingDecoder decoder, int actingVersion) {
        long result0 = decoder.mantissa();
        byte result1 = decoder.exponent();
        return new OptionalDecimalEncoding(result0, result1);
    }
}

