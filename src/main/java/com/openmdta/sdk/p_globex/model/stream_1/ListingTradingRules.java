package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record ListingTradingRules(java.util.List<Byte> tradingModel, java.util.List<Byte> closedBook, java.util.List<Byte> marketImbalance, java.util.List<Byte> auctionType, java.util.List<Byte> quotingPeriodStart, java.util.List<Byte> quotingPeriodEnd, java.util.List<Byte> singleSidedQuotes, java.util.List<Byte> crossMatchDefault) {
    public ListingTradingRules {
        tradingModel = java.util.List.copyOf(tradingModel);
        closedBook = java.util.List.copyOf(closedBook);
        marketImbalance = java.util.List.copyOf(marketImbalance);
        auctionType = java.util.List.copyOf(auctionType);
        quotingPeriodStart = java.util.List.copyOf(quotingPeriodStart);
        quotingPeriodEnd = java.util.List.copyOf(quotingPeriodEnd);
        singleSidedQuotes = java.util.List.copyOf(singleSidedQuotes);
        crossMatchDefault = java.util.List.copyOf(crossMatchDefault);
    }
    public static ListingTradingRules decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingTradingRulesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        byte[] value0Bytes = new byte[decoder.tradingModelLength()];
        decoder.getTradingModel(value0Bytes, 0, value0Bytes.length);
        var value0 = new java.util.ArrayList<Byte>(value0Bytes.length);
        for (byte item : value0Bytes) { value0.add(item); }
        java.util.List<Byte> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.closedBookLength()];
        decoder.getClosedBook(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        byte[] value2Bytes = new byte[decoder.marketImbalanceLength()];
        decoder.getMarketImbalance(value2Bytes, 0, value2Bytes.length);
        var value2 = new java.util.ArrayList<Byte>(value2Bytes.length);
        for (byte item : value2Bytes) { value2.add(item); }
        java.util.List<Byte> result2 = java.util.List.copyOf(value2);
        byte[] value3Bytes = new byte[decoder.auctionTypeLength()];
        decoder.getAuctionType(value3Bytes, 0, value3Bytes.length);
        var value3 = new java.util.ArrayList<Byte>(value3Bytes.length);
        for (byte item : value3Bytes) { value3.add(item); }
        java.util.List<Byte> result3 = java.util.List.copyOf(value3);
        byte[] value4Bytes = new byte[decoder.quotingPeriodStartLength()];
        decoder.getQuotingPeriodStart(value4Bytes, 0, value4Bytes.length);
        var value4 = new java.util.ArrayList<Byte>(value4Bytes.length);
        for (byte item : value4Bytes) { value4.add(item); }
        java.util.List<Byte> result4 = java.util.List.copyOf(value4);
        byte[] value5Bytes = new byte[decoder.quotingPeriodEndLength()];
        decoder.getQuotingPeriodEnd(value5Bytes, 0, value5Bytes.length);
        var value5 = new java.util.ArrayList<Byte>(value5Bytes.length);
        for (byte item : value5Bytes) { value5.add(item); }
        java.util.List<Byte> result5 = java.util.List.copyOf(value5);
        byte[] value6Bytes = new byte[decoder.singleSidedQuotesLength()];
        decoder.getSingleSidedQuotes(value6Bytes, 0, value6Bytes.length);
        var value6 = new java.util.ArrayList<Byte>(value6Bytes.length);
        for (byte item : value6Bytes) { value6.add(item); }
        java.util.List<Byte> result6 = java.util.List.copyOf(value6);
        byte[] value7Bytes = new byte[decoder.crossMatchDefaultLength()];
        decoder.getCrossMatchDefault(value7Bytes, 0, value7Bytes.length);
        var value7 = new java.util.ArrayList<Byte>(value7Bytes.length);
        for (byte item : value7Bytes) { value7.add(item); }
        java.util.List<Byte> result7 = java.util.List.copyOf(value7);
        return new ListingTradingRules(result0, result1, result2, result3, result4, result5, result6, result7);
    }
}

