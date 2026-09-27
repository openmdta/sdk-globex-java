package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record InstrumentDefinition(com.openmdta.sdk.p_globex.sbe.stream_0.ListingActivity activity, java.util.List<Byte> instrument, java.util.List<Byte> isin, java.util.List<Byte> venue, java.util.List<Byte> currency, java.util.List<Byte> name) {
    public InstrumentDefinition {
        instrument = java.util.List.copyOf(instrument);
        isin = java.util.List.copyOf(isin);
        venue = java.util.List.copyOf(venue);
        currency = java.util.List.copyOf(currency);
        name = java.util.List.copyOf(name);
    }
    public static InstrumentDefinition decode(com.openmdta.sdk.p_globex.sbe.stream_0.InstrumentDefinitionDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        com.openmdta.sdk.p_globex.sbe.stream_0.ListingActivity result0 = decoder.activity();
        byte[] value1Bytes = new byte[decoder.instrumentLength()];
        decoder.getInstrument(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.isinLength()];
        decoder.getIsin(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.venueLength()];
        decoder.getVenue(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        byte[] value4Bytes = new byte[decoder.currencyLength()];
        decoder.getCurrency(value4Bytes, 0, value4Bytes.length);
        var value4 = new java.util.ArrayList<Byte>(value4Bytes.length);
        for (byte item : value4Bytes) { value4.add(item); }
        java.util.List<Byte> result4 = java.util.List.copyOf(value4);
        byte[] value5Bytes = new byte[decoder.nameLength()];
        decoder.getName(value5Bytes, 0, value5Bytes.length);
        var value5 = new java.util.ArrayList<Byte>(value5Bytes.length);
        for (byte item : value5Bytes) { value5.add(item); }
        java.util.List<Byte> result5 = java.util.List.copyOf(value5);
        return new InstrumentDefinition(result0, result1, result2, result3, result4, result5);
    }
}

