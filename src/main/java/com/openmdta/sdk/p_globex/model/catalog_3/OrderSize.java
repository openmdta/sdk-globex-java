package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record OrderSize(String minimum_tradable_unit, String minimum_order_quantity) {
    public static OrderSize decode(com.openmdta.sdk.p_globex.sbe.catalog_3.OrderSizeDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.minimum_tradable_unit();
        String result1 = decoder.minimum_order_quantity();
        return new OrderSize(result0, result1);
    }
}

