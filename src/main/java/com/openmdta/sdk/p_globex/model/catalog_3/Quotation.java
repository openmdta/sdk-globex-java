package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Quotation(String number_of_decimal_digits, String unit_of_quotation, String currency) {
    public static Quotation decode(com.openmdta.sdk.p_globex.sbe.catalog_3.QuotationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.number_of_decimal_digits();
        String result1 = decoder.unit_of_quotation();
        String result2 = decoder.currency();
        return new Quotation(result0, result1, result2);
    }
}

