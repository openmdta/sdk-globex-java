package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record ListingVenue(java.util.List<Byte> mic, java.util.List<Byte> primaryMic, java.util.List<Byte> reportingMarket, java.util.List<Byte> offBookReportingMarket) {
    public ListingVenue {
        mic = java.util.List.copyOf(mic);
        primaryMic = java.util.List.copyOf(primaryMic);
        reportingMarket = java.util.List.copyOf(reportingMarket);
        offBookReportingMarket = java.util.List.copyOf(offBookReportingMarket);
    }
    public static ListingVenue decode(com.openmdta.sdk.p_globex.sbe.stream_0.ListingVenueDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.micLength()];
        decoder.getMic(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.primaryMicLength()];
        decoder.getPrimaryMic(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.reportingMarketLength()];
        decoder.getReportingMarket(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.offBookReportingMarketLength()];
        decoder.getOffBookReportingMarket(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        return new ListingVenue(result0, result1, result2, result3);
    }
}

