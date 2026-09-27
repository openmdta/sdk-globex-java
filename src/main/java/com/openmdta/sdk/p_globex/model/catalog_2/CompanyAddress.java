package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record CompanyAddress(String language, String firstAddressLine, String additionalAddressLine1, String additionalAddressLine2, String additionalAddressLine3, String addressNumber, String addressNumberWithinBuilding, String mailRouting, String city, String region, String country, String postalCode) {
    public static CompanyAddress decode(com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyAddressDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.language();
        String result1 = decoder.firstAddressLine();
        String result2 = decoder.additionalAddressLine1();
        String result3 = decoder.additionalAddressLine2();
        String result4 = decoder.additionalAddressLine3();
        String result5 = decoder.addressNumber();
        String result6 = decoder.addressNumberWithinBuilding();
        String result7 = decoder.mailRouting();
        String result8 = decoder.city();
        String result9 = decoder.region();
        String result10 = decoder.country();
        String result11 = decoder.postalCode();
        return new CompanyAddress(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11);
    }
}

