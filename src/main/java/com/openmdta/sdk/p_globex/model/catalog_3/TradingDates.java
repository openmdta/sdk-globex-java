package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TradingDates(String first_trading_date, String last_trading_date) {
    public static TradingDates decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TradingDatesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.first_trading_date();
        String result1 = decoder.last_trading_date();
        return new TradingDates(result0, result1);
    }
}

