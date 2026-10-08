package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListingQuoteParameters(String priceRange, String priceRangePercent, String minimumSize) {
    public static ListingQuoteParameters decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListingQuoteParametersDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.priceRange();
        String result1 = decoder.priceRangePercent();
        String result2 = decoder.minimumSize();
        return new ListingQuoteParameters(result0, result1, result2);
    }
}

