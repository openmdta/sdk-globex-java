package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record Classification(java.util.List<Byte> code, java.util.List<Byte> label) {
    public Classification {
        code = java.util.List.copyOf(code);
        label = java.util.List.copyOf(label);
    }
    public static Classification decode(com.openmdta.sdk.p_globex.sbe.stream_1.ClassificationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.codeLength()];
        decoder.getCode(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.labelLength()];
        decoder.getLabel(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        return new Classification(result0, result1);
    }
}

