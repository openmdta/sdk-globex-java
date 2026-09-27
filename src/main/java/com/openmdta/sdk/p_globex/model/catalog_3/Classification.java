package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Classification(String product_assignment_group, String product_assignment_group_description, String instrument_type, String market_segment, String market_segment_supplement, String security_sub_type, String security_classification_value) {
    public static Classification decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.product_assignment_group();
        String result1 = decoder.product_assignment_group_description();
        String result2 = decoder.instrument_type();
        String result3 = decoder.market_segment();
        String result4 = decoder.market_segment_supplement();
        String result5 = decoder.security_sub_type();
        String result6 = decoder.security_classification_value();
        return new Classification(result0, result1, result2, result3, result4, result5, result6);
    }
}

