package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record OrderRiskLimits(String maximum_order_quantity, String maximum_order_value) {
    public static OrderRiskLimits decode(com.openmdta.sdk.p_globex.sbe.catalog_3.OrderRiskLimitsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.maximum_order_quantity();
        String result1 = decoder.maximum_order_value();
        return new OrderRiskLimits(result0, result1);
    }
}

