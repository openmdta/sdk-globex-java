package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListingOrderSize(String minimumTradableUnit, String minimumOrderQuantity) {
    public static ListingOrderSize decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListingOrderSizeDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.minimumTradableUnit();
        String result1 = decoder.minimumOrderQuantity();
        return new ListingOrderSize(result0, result1);
    }
}

