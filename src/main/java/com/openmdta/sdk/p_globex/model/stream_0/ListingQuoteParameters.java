package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record ListingQuoteParameters(java.util.List<Byte> priceRange, java.util.List<Byte> priceRangePercent, java.util.List<Byte> minimumSize) {
    public ListingQuoteParameters {
        priceRange = java.util.List.copyOf(priceRange);
        priceRangePercent = java.util.List.copyOf(priceRangePercent);
        minimumSize = java.util.List.copyOf(minimumSize);
    }
    public static ListingQuoteParameters decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListingQuoteParametersDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.priceRangeLength()];
        decoder.getPriceRange(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.priceRangePercentLength()];
        decoder.getPriceRangePercent(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.minimumSizeLength()];
        decoder.getMinimumSize(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        return new ListingQuoteParameters(result0, result1, result2);
    }
}

