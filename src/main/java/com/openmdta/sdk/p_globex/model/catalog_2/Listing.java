package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record Listing(String source, String venue, String instrument, String currency, String isin) {
    public static Listing decode(com.openmdta.sdk.p_globex.sbe.catalog_2.ListingDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.source();
        String result1 = decoder.venue();
        String result2 = decoder.instrument();
        String result3 = decoder.currency();
        String result4 = decoder.isin();
        return new Listing(result0, result1, result2, result3, result4);
    }
}

