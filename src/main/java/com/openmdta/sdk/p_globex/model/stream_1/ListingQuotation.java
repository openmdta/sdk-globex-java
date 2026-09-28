package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record ListingQuotation(java.util.List<Byte> currency, java.util.List<Byte> unit, java.util.List<Byte> decimalDigits, java.util.List<Byte> symbol) {
    public ListingQuotation {
        currency = java.util.List.copyOf(currency);
        unit = java.util.List.copyOf(unit);
        decimalDigits = java.util.List.copyOf(decimalDigits);
        symbol = java.util.List.copyOf(symbol);
    }
    public static ListingQuotation decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingQuotationDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.currencyLength()];
        decoder.getCurrency(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.unitLength()];
        decoder.getUnit(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.decimalDigitsLength()];
        decoder.getDecimalDigits(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.symbolLength()];
        decoder.getSymbol(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        return new ListingQuotation(result0, result1, result2, result3);
    }
}

