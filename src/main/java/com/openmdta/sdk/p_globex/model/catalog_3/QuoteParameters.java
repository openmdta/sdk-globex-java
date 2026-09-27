package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record QuoteParameters(String price_range_value, String price_range_percentage, String minimum_quote_size) {
    public static QuoteParameters decode(com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteParametersDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.price_range_value();
        String result1 = decoder.price_range_percentage();
        String result2 = decoder.minimum_quote_size();
        return new QuoteParameters(result0, result1, result2);
    }
}

