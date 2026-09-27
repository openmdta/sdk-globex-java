package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TradingVenue(String mic_code, String primary_market_mic_code, String reporting_market, String off_book_reporting_market) {
    public static TradingVenue decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TradingVenueDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.mic_code();
        String result1 = decoder.primary_market_mic_code();
        String result2 = decoder.reporting_market();
        String result3 = decoder.off_book_reporting_market();
        return new TradingVenue(result0, result1, result2, result3);
    }
}

