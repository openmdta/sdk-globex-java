package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record InstrumentNames(java.util.List<Localized> localized, String long_, String short_) {
    public InstrumentNames {
        localized = java.util.List.copyOf(localized);
    }
    public static InstrumentNames decode(com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentNamesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Localized>();
        var value0Group = decoder.localized();
        while (value0Group.hasNext()) {
            value0.add(Localized.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Localized> result0 = java.util.List.copyOf(value0);
        String result1 = decoder.long_();
        String result2 = decoder.short_();
        return new InstrumentNames(result0, result1, result2);
    }
public record Localized(String language, String long_, String short_) {
    public static Localized decode(com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentNamesDecoder.LocalizedDecoder decoder, int actingVersion) {
        String result0 = decoder.language();
        String result1 = decoder.long_();
        String result2 = decoder.short_();
        return new Localized(result0, result1, result2);
    }
}
}

