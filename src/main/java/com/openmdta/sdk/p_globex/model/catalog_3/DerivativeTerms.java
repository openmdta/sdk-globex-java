package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record DerivativeTerms(String underlying, String strike_price, String warrant_type, String cover_indicator) {
    public static DerivativeTerms decode(com.openmdta.sdk.p_globex.sbe.catalog_3.DerivativeTermsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.underlying();
        String result1 = decoder.strike_price();
        String result2 = decoder.warrant_type();
        String result3 = decoder.cover_indicator();
        return new DerivativeTerms(result0, result1, result2, result3);
    }
}

