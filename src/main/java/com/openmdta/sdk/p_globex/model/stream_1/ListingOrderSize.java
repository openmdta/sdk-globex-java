package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record ListingOrderSize(java.util.List<Byte> minimumTradableUnit, java.util.List<Byte> minimumOrderQuantity) {
    public ListingOrderSize {
        minimumTradableUnit = java.util.List.copyOf(minimumTradableUnit);
        minimumOrderQuantity = java.util.List.copyOf(minimumOrderQuantity);
    }
    public static ListingOrderSize decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingOrderSizeDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.minimumTradableUnitLength()];
        decoder.getMinimumTradableUnit(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.minimumOrderQuantityLength()];
        decoder.getMinimumOrderQuantity(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        return new ListingOrderSize(result0, result1);
    }
}

