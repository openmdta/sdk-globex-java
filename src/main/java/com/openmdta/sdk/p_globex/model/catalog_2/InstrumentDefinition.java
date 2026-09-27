package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record InstrumentDefinition(com.openmdta.sdk.p_globex.sbe.catalog_2.ListingActivity activity, String instrument, String isin, String venue, String currency, String name) {
    public static InstrumentDefinition decode(com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentDefinitionDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        com.openmdta.sdk.p_globex.sbe.catalog_2.ListingActivity result0 = decoder.activity();
        String result1 = decoder.instrument();
        String result2 = decoder.isin();
        String result3 = decoder.venue();
        String result4 = decoder.currency();
        String result5 = decoder.name();
        return new InstrumentDefinition(result0, result1, result2, result3, result4, result5);
    }
}

