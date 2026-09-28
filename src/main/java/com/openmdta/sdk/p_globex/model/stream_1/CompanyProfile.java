package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record CompanyProfile(short hasLei, java.util.List<Byte> canonicalName, java.util.List<Byte> lei) {
    public CompanyProfile {
        canonicalName = java.util.List.copyOf(canonicalName);
        lei = java.util.List.copyOf(lei);
    }
    public static CompanyProfile decode(com.openmdta.sdk.p_globex.sbe.stream_1.CompanyProfileDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        short result0 = decoder.hasLei();
        byte[] value1Bytes = new byte[decoder.canonicalNameLength()];
        decoder.getCanonicalName(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.leiLength()];
        decoder.getLei(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        return new CompanyProfile(result0, result1, result2);
    }
}

