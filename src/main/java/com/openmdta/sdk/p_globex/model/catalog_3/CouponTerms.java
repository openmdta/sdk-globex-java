package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record CouponTerms(String flat_indicator, String coupon_rate, String previous_coupon_payment_date, String next_coupon_payment_date, String pool_factor, String indexation_coefficient, String accrued_interest_calculation_method) {
    public static CouponTerms decode(com.openmdta.sdk.p_globex.sbe.catalog_3.CouponTermsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.flat_indicator();
        String result1 = decoder.coupon_rate();
        String result2 = decoder.previous_coupon_payment_date();
        String result3 = decoder.next_coupon_payment_date();
        String result4 = decoder.pool_factor();
        String result5 = decoder.indexation_coefficient();
        String result6 = decoder.accrued_interest_calculation_method();
        return new CouponTerms(result0, result1, result2, result3, result4, result5, result6);
    }
}

