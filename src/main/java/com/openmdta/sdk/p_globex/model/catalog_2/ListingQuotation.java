package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record ListingQuotation(String currency, String unit, String decimalDigits, String symbol) {
    public static ListingQuotation decode(com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.currency();
        String result1 = decoder.unit();
        String result2 = decoder.decimalDigits();
        String result3 = decoder.symbol();
        return new ListingQuotation(result0, result1, result2, result3);
    }
}

