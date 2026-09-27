package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record ListDefinition(java.util.List<Members> members, java.util.List<Variants> variants, java.util.List<Byte> code, java.util.List<Byte> name, java.util.List<Byte> description, java.util.List<Byte> kind, java.util.List<Byte> memberDimension, java.util.List<Byte> validAt) {
    public ListDefinition {
        members = java.util.List.copyOf(members);
        variants = java.util.List.copyOf(variants);
        code = java.util.List.copyOf(code);
        name = java.util.List.copyOf(name);
        description = java.util.List.copyOf(description);
        kind = java.util.List.copyOf(kind);
        memberDimension = java.util.List.copyOf(memberDimension);
        validAt = java.util.List.copyOf(validAt);
    }
    public static ListDefinition decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListDefinitionDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Members>();
        var value0Group = decoder.members();
        while (value0Group.hasNext()) {
            value0.add(Members.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Members> result0 = java.util.List.copyOf(value0);
        var value1 = new java.util.ArrayList<Variants>();
        var value1Group = decoder.variants();
        while (value1Group.hasNext()) {
            value1.add(Variants.decode(value1Group.next(), actingVersion));
        }
        java.util.List<Variants> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.codeLength()];
        decoder.getCode(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.nameLength()];
        decoder.getName(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        byte[] value4Bytes = new byte[decoder.descriptionLength()];
        decoder.getDescription(value4Bytes, 0, value4Bytes.length);
        var value4 = new java.util.ArrayList<Byte>(value4Bytes.length);
        for (byte item : value4Bytes) { value4.add(item); }
        java.util.List<Byte> result4 = java.util.List.copyOf(value4);
        byte[] value5Bytes = new byte[decoder.kindLength()];
        decoder.getKind(value5Bytes, 0, value5Bytes.length);
        var value5 = new java.util.ArrayList<Byte>(value5Bytes.length);
        for (byte item : value5Bytes) { value5.add(item); }
        java.util.List<Byte> result5 = java.util.List.copyOf(value5);
        byte[] value6Bytes = new byte[decoder.memberDimensionLength()];
        decoder.getMemberDimension(value6Bytes, 0, value6Bytes.length);
        var value6 = new java.util.ArrayList<Byte>(value6Bytes.length);
        for (byte item : value6Bytes) { value6.add(item); }
        java.util.List<Byte> result6 = java.util.List.copyOf(value6);
        byte[] value7Bytes = new byte[decoder.validAtLength()];
        decoder.getValidAt(value7Bytes, 0, value7Bytes.length);
        var value7 = new java.util.ArrayList<Byte>(value7Bytes.length);
        for (byte item : value7Bytes) { value7.add(item); }
        java.util.List<Byte> result7 = java.util.List.copyOf(value7);
        return new ListDefinition(result0, result1, result2, result3, result4, result5, result6, result7);
    }
public record Members(java.util.List<Byte> identifier) {
    public Members {
        identifier = java.util.List.copyOf(identifier);
    }
    public static Members decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListDefinitionDecoder.MembersDecoder decoder, int actingVersion) {
        byte[] value0Bytes = new byte[decoder.identifierLength()];
        decoder.getIdentifier(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        return new Members(result0);
    }
}
public record Variants(java.util.List<Byte> returnType, java.util.List<Byte> currency, java.util.List<Byte> identifier) {
    public Variants {
        returnType = java.util.List.copyOf(returnType);
        currency = java.util.List.copyOf(currency);
        identifier = java.util.List.copyOf(identifier);
    }
    public static Variants decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListDefinitionDecoder.VariantsDecoder decoder, int actingVersion) {
        byte[] value0Bytes = new byte[decoder.returnTypeLength()];
        decoder.getReturnType(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.currencyLength()];
        decoder.getCurrency(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.identifierLength()];
        decoder.getIdentifier(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        return new Variants(result0, result1, result2);
    }
}
}

