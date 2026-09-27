package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record LegalEntityNames(java.util.List<Localized> localized, java.util.List<Byte> long_, java.util.List<Byte> short_) {
    public LegalEntityNames {
        localized = java.util.List.copyOf(localized);
        long_ = java.util.List.copyOf(long_);
        short_ = java.util.List.copyOf(short_);
    }
    public static LegalEntityNames decode(com.openmdta.sdk.p_globex.sbe.stream_0.LegalEntityNamesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Localized>();
        var value0Group = decoder.localized();
        while (value0Group.hasNext()) {
            value0.add(Localized.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Localized> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.long_Length()];
        decoder.getLong_(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.short_Length()];
        decoder.getShort_(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        return new LegalEntityNames(result0, result1, result2);
    }
public record Localized(java.util.List<Byte> language, java.util.List<Byte> long_, java.util.List<Byte> short_) {
    public Localized {
        language = java.util.List.copyOf(language);
        long_ = java.util.List.copyOf(long_);
        short_ = java.util.List.copyOf(short_);
    }
    public static Localized decode(com.openmdta.sdk.p_globex.sbe.stream_0.LegalEntityNamesDecoder.LocalizedDecoder decoder, int actingVersion) {
        byte[] value0Bytes = new byte[decoder.languageLength()];
        decoder.getLanguage(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.long_Length()];
        decoder.getLong_(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.short_Length()];
        decoder.getShort_(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        return new Localized(result0, result1, result2);
    }
}
}

