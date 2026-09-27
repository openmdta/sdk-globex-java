package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Lifecycle(String product_status, String instrument_status, String in_subscription, String disable_on_book_trading) {
    public static Lifecycle decode(com.openmdta.sdk.p_globex.sbe.catalog_3.LifecycleDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.product_status();
        String result1 = decoder.instrument_status();
        String result2 = decoder.in_subscription();
        String result3 = decoder.disable_on_book_trading();
        return new Lifecycle(result0, result1, result2, result3);
    }
}

