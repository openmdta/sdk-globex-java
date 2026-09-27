package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record XetraRecord(int presence, com.openmdta.sdk.p_globex.sbe.catalog_3.SourceListingStatus listingStatus, String isin, String instrumentName, String mic, String currency, String instrumentId, String wkn, String mnemonic, String productStatus, String instrumentStatus, String priceDecimals) {
    public static XetraRecord decode(com.openmdta.sdk.p_globex.sbe.catalog_3.XetraRecordDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        int result0 = decoder.presence();
        com.openmdta.sdk.p_globex.sbe.catalog_3.SourceListingStatus result1 = decoder.listingStatus();
        String result2 = decoder.isin();
        String result3 = decoder.instrumentName();
        String result4 = decoder.mic();
        String result5 = decoder.currency();
        String result6 = decoder.instrumentId();
        String result7 = decoder.wkn();
        String result8 = decoder.mnemonic();
        String result9 = decoder.productStatus();
        String result10 = decoder.instrumentStatus();
        String result11 = decoder.priceDecimals();
        return new XetraRecord(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11);
    }
}

