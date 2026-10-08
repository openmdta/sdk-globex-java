package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record ListingTradingRules(String tradingModel, String closedBook, String marketImbalance, String auctionType, String quotingPeriodStart, String quotingPeriodEnd, String singleSidedQuotes, String crossMatchDefault) {
    public static ListingTradingRules decode(com.openmdta.sdk.p_globex.sbe.catalog_3.ListingTradingRulesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.tradingModel();
        String result1 = decoder.closedBook();
        String result2 = decoder.marketImbalance();
        String result3 = decoder.auctionType();
        String result4 = decoder.quotingPeriodStart();
        String result5 = decoder.quotingPeriodEnd();
        String result6 = decoder.singleSidedQuotes();
        String result7 = decoder.crossMatchDefault();
        return new ListingTradingRules(result0, result1, result2, result3, result4, result5, result6, result7);
    }
}

