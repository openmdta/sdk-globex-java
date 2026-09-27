package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record InstrumentIssuer(java.util.List<Byte> lei) {
    public InstrumentIssuer {
        lei = java.util.List.copyOf(lei);
    }
    public static InstrumentIssuer decode(com.openmdta.sdk.p_globex.sbe.stream_0.InstrumentIssuerDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.leiLength()];
        decoder.getLei(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        return new InstrumentIssuer(result0);
    }
}

