package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record CompanyAddress(java.util.List<Byte> language, java.util.List<Byte> firstAddressLine, java.util.List<Byte> additionalAddressLine1, java.util.List<Byte> additionalAddressLine2, java.util.List<Byte> additionalAddressLine3, java.util.List<Byte> addressNumber, java.util.List<Byte> addressNumberWithinBuilding, java.util.List<Byte> mailRouting, java.util.List<Byte> city, java.util.List<Byte> region, java.util.List<Byte> country, java.util.List<Byte> postalCode) {
    public CompanyAddress {
        language = java.util.List.copyOf(language);
        firstAddressLine = java.util.List.copyOf(firstAddressLine);
        additionalAddressLine1 = java.util.List.copyOf(additionalAddressLine1);
        additionalAddressLine2 = java.util.List.copyOf(additionalAddressLine2);
        additionalAddressLine3 = java.util.List.copyOf(additionalAddressLine3);
        addressNumber = java.util.List.copyOf(addressNumber);
        addressNumberWithinBuilding = java.util.List.copyOf(addressNumberWithinBuilding);
        mailRouting = java.util.List.copyOf(mailRouting);
        city = java.util.List.copyOf(city);
        region = java.util.List.copyOf(region);
        country = java.util.List.copyOf(country);
        postalCode = java.util.List.copyOf(postalCode);
    }
    public static CompanyAddress decode(com.openmdta.sdk.p_globex.sbe.stream_0.CompanyAddressDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.languageLength()];
        decoder.getLanguage(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.firstAddressLineLength()];
        decoder.getFirstAddressLine(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.additionalAddressLine1Length()];
        decoder.getAdditionalAddressLine1(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.additionalAddressLine2Length()];
        decoder.getAdditionalAddressLine2(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        byte[] value4Bytes = new byte[decoder.additionalAddressLine3Length()];
        decoder.getAdditionalAddressLine3(value4Bytes, 0, value4Bytes.length);
        var value4 = new java.util.ArrayList<Byte>(value4Bytes.length);
        for (byte item : value4Bytes) { value4.add(item); }
        java.util.List<Byte> result4 = java.util.List.copyOf(value4);
        byte[] value5Bytes = new byte[decoder.addressNumberLength()];
        decoder.getAddressNumber(value5Bytes, 0, value5Bytes.length);
        var value5 = new java.util.ArrayList<Byte>(value5Bytes.length);
        for (byte item : value5Bytes) { value5.add(item); }
        java.util.List<Byte> result5 = java.util.List.copyOf(value5);
        byte[] value6Bytes = new byte[decoder.addressNumberWithinBuildingLength()];
        decoder.getAddressNumberWithinBuilding(value6Bytes, 0, value6Bytes.length);
        var value6 = new java.util.ArrayList<Byte>(value6Bytes.length);
        for (byte item : value6Bytes) { value6.add(item); }
        java.util.List<Byte> result6 = java.util.List.copyOf(value6);
        byte[] value7Bytes = new byte[decoder.mailRoutingLength()];
        decoder.getMailRouting(value7Bytes, 0, value7Bytes.length);
        var value7 = new java.util.ArrayList<Byte>(value7Bytes.length);
        for (byte item : value7Bytes) { value7.add(item); }
        java.util.List<Byte> result7 = java.util.List.copyOf(value7);
        byte[] value8Bytes = new byte[decoder.cityLength()];
        decoder.getCity(value8Bytes, 0, value8Bytes.length);
        var value8 = new java.util.ArrayList<Byte>(value8Bytes.length);
        for (byte item : value8Bytes) { value8.add(item); }
        java.util.List<Byte> result8 = java.util.List.copyOf(value8);
        byte[] value9Bytes = new byte[decoder.regionLength()];
        decoder.getRegion(value9Bytes, 0, value9Bytes.length);
        var value9 = new java.util.ArrayList<Byte>(value9Bytes.length);
        for (byte item : value9Bytes) { value9.add(item); }
        java.util.List<Byte> result9 = java.util.List.copyOf(value9);
        byte[] value10Bytes = new byte[decoder.countryLength()];
        decoder.getCountry(value10Bytes, 0, value10Bytes.length);
        var value10 = new java.util.ArrayList<Byte>(value10Bytes.length);
        for (byte item : value10Bytes) { value10.add(item); }
        java.util.List<Byte> result10 = java.util.List.copyOf(value10);
        byte[] value11Bytes = new byte[decoder.postalCodeLength()];
        decoder.getPostalCode(value11Bytes, 0, value11Bytes.length);
        var value11 = new java.util.ArrayList<Byte>(value11Bytes.length);
        for (byte item : value11Bytes) { value11.add(item); }
        java.util.List<Byte> result11 = java.util.List.copyOf(value11);
        return new CompanyAddress(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11);
    }
}

