package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TradingRules(String trading_model_type, String closed_book_indicator, String market_imbalance_indicator, String instrument_auction_type, String quoting_period_start, String quoting_period_end, String single_sided_quote_support, String cross_match_instruction_default) {
    public static TradingRules decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TradingRulesDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.trading_model_type();
        String result1 = decoder.closed_book_indicator();
        String result2 = decoder.market_imbalance_indicator();
        String result3 = decoder.instrument_auction_type();
        String result4 = decoder.quoting_period_start();
        String result5 = decoder.quoting_period_end();
        String result6 = decoder.single_sided_quote_support();
        String result7 = decoder.cross_match_instruction_default();
        return new TradingRules(result0, result1, result2, result3, result4, result5, result6, result7);
    }
}

