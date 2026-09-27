package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record ListingVenue(String mic, String primaryMic, String reportingMarket, String offBookReportingMarket) {
    public static ListingVenue decode(com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.mic();
        String result1 = decoder.primaryMic();
        String result2 = decoder.reportingMarket();
        String result3 = decoder.offBookReportingMarket();
        return new ListingVenue(result0, result1, result2, result3);
    }
}

