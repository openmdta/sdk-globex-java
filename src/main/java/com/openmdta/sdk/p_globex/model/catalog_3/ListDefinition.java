package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListDefinition(java.util.List<Members> members, java.util.List<Variants> variants, String code, String name, String description, String kind, String memberDimension, String validAt) {
    public ListDefinition {
        members = java.util.List.copyOf(members);
        variants = java.util.List.copyOf(variants);
    }
    public static ListDefinition decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListDefinitionDecoder decoder) {
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
        String result2 = decoder.code();
        String result3 = decoder.name();
        String result4 = decoder.description();
        String result5 = decoder.kind();
        String result6 = decoder.memberDimension();
        String result7 = decoder.validAt();
        return new ListDefinition(result0, result1, result2, result3, result4, result5, result6, result7);
    }
public record Members(String identifier) {
    public static Members decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListDefinitionDecoder.MembersDecoder decoder, int actingVersion) {
        String result0 = decoder.identifier();
        return new Members(result0);
    }
}
public record Variants(String returnType, String currency, String identifier) {
    public static Variants decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListDefinitionDecoder.VariantsDecoder decoder, int actingVersion) {
        String result0 = decoder.returnType();
        String result1 = decoder.currency();
        String result2 = decoder.identifier();
        return new Variants(result0, result1, result2);
    }
}
}

