package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListingTradingDates(String firstTradingDate, String lastTradingDate) {
    public static ListingTradingDates decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListingTradingDatesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.firstTradingDate();
        String result1 = decoder.lastTradingDate();
        return new ListingTradingDates(result0, result1);
    }
}

