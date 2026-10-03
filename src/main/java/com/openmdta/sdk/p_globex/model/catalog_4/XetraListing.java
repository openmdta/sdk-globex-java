package com.openmdta.sdk.p_globex.model.catalog_4;

/** Immutable owned value; decoding allocates. */
public record XetraListing(String productId, String productStatus, String instrumentStatus, String inSubscription, String disableOnBookTrading, String midpointTrading, String midpointExecutionVenueId, String ccpEligibleCode, String clearingLocation, String settlementPeriod, String settlementCurrency, String multiCcpEligible, String depositType, String maximumOrderQuantity, String maximumOrderValue, String minimumIcebergTotalVolume, String minimumIcebergDisplayVolume) {
    public static XetraListing decode(com.openmdta.sdk.p_globex.sbe.catalog_4.XetraListingDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.productId();
        String result1 = decoder.productStatus();
        String result2 = decoder.instrumentStatus();
        String result3 = decoder.inSubscription();
        String result4 = decoder.disableOnBookTrading();
        String result5 = decoder.midpointTrading();
        String result6 = decoder.midpointExecutionVenueId();
        String result7 = decoder.ccpEligibleCode();
        String result8 = decoder.clearingLocation();
        String result9 = decoder.settlementPeriod();
        String result10 = decoder.settlementCurrency();
        String result11 = decoder.multiCcpEligible();
        String result12 = decoder.depositType();
        String result13 = decoder.maximumOrderQuantity();
        String result14 = decoder.maximumOrderValue();
        String result15 = decoder.minimumIcebergTotalVolume();
        String result16 = decoder.minimumIcebergDisplayVolume();
        return new XetraListing(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11, result12, result13, result14, result15, result16);
    }
}

