package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record ListingTradingDates(java.util.List<Byte> firstTradingDate, java.util.List<Byte> lastTradingDate) {
    public ListingTradingDates {
        firstTradingDate = java.util.List.copyOf(firstTradingDate);
        lastTradingDate = java.util.List.copyOf(lastTradingDate);
    }
    public static ListingTradingDates decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListingTradingDatesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.firstTradingDateLength()];
        decoder.getFirstTradingDate(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.lastTradingDateLength()];
        decoder.getLastTradingDate(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        return new ListingTradingDates(result0, result1);
    }
}

