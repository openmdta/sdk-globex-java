package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record BasicMasterdata(java.util.List<Byte> name) {
    public BasicMasterdata {
        name = java.util.List.copyOf(name);
    }
    public static BasicMasterdata decode(com.openmdta.sdk.p_globex.sbe.stream_1.BasicMasterdataDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.nameLength()];
        decoder.getName(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        return new BasicMasterdata(result0);
    }
}

