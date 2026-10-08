package com.openmdta.sdk.p_globex.model.catalog_0;

/** Immutable owned value; decoding allocates. */
public record FirdsRecord(int presence, com.openmdta.sdk.p_globex.sbe.catalog_0.SourceChange change, java.util.OptionalInt commodityDerivative, java.util.OptionalInt issuerRequestedAdmission, String isin, String fullName, String shortName, String cfi, String currency, String issuerLei, String mic, String firstTradeDate, String terminationDate, String relevantCompetentAuthority, String publicationFrom, String relevantTradingVenue) {
    public static FirdsRecord decode(com.openmdta.sdk.p_globex.sbe.catalog_0.FirdsRecordDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        int result0 = decoder.presence();
        com.openmdta.sdk.p_globex.sbe.catalog_0.SourceChange result1 = decoder.change();
        java.util.OptionalInt result2 = (decoder.commodityDerivative() == com.openmdta.sdk.p_globex.sbe.catalog_0.FirdsRecordDecoder.commodityDerivativeNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.commodityDerivative());
        java.util.OptionalInt result3 = (decoder.issuerRequestedAdmission() == com.openmdta.sdk.p_globex.sbe.catalog_0.FirdsRecordDecoder.issuerRequestedAdmissionNullValue()) ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(decoder.issuerRequestedAdmission());
        String result4 = decoder.isin();
        String result5 = decoder.fullName();
        String result6 = decoder.shortName();
        String result7 = decoder.cfi();
        String result8 = decoder.currency();
        String result9 = decoder.issuerLei();
        String result10 = decoder.mic();
        String result11 = decoder.firstTradeDate();
        String result12 = decoder.terminationDate();
        String result13 = decoder.relevantCompetentAuthority();
        String result14 = decoder.publicationFrom();
        String result15 = decoder.relevantTradingVenue();
        return new FirdsRecord(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11, result12, result13, result14, result15);
    }
}

