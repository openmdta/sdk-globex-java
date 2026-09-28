package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record Listing(java.util.List<Byte> source, java.util.List<Byte> venue, java.util.List<Byte> instrument, java.util.List<Byte> currency, java.util.List<Byte> isin) {
    public Listing {
        source = java.util.List.copyOf(source);
        venue = java.util.List.copyOf(venue);
        instrument = java.util.List.copyOf(instrument);
        currency = java.util.List.copyOf(currency);
        isin = java.util.List.copyOf(isin);
    }
    public static Listing decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.sourceLength()];
        decoder.getSource(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.venueLength()];
        decoder.getVenue(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.instrumentLength()];
        decoder.getInstrument(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.currencyLength()];
        decoder.getCurrency(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        byte[] value4Bytes = new byte[decoder.isinLength()];
        decoder.getIsin(value4Bytes, 0, value4Bytes.length);
        var value4 = new java.util.ArrayList<Byte>(value4Bytes.length);
        for (byte item : value4Bytes) { value4.add(item); }
        java.util.List<Byte> result4 = java.util.List.copyOf(value4);
        return new Listing(result0, result1, result2, result3, result4);
    }
}

