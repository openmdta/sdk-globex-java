package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record TickSizeTable(String tick_size_1, String upper_price_limit_max, String tick_size_2, String upper_price_limit_2, String tick_size_3, String upper_price_limit_3, String tick_size_4, String upper_price_limit_4, String tick_size_5, String upper_price_limit_5, String tick_size_6, String upper_price_limit_6, String tick_size_7, String upper_price_limit_7, String tick_size_8, String upper_price_limit_8, String tick_size_9, String upper_price_limit_9, String tick_size_10, String upper_price_limit_10, String tick_size_11, String upper_price_limit_11, String tick_size_12, String upper_price_limit_12, String tick_size_13, String upper_price_limit_13, String tick_size_14, String upper_price_limit_14, String tick_size_15, String upper_price_limit_15, String tick_size_16, String upper_price_limit_16, String tick_size_17, String upper_price_limit_17, String tick_size_18, String upper_price_limit_18, String tick_size_19, String upper_price_limit_19, String tick_size_20, String upper_price_limit_20, String tick_size_band) {
    public static TickSizeTable decode(com.openmdta.sdk.p_globex.sbe.catalog_3.TickSizeTableDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.tick_size_1();
        String result1 = decoder.upper_price_limit_max();
        String result2 = decoder.tick_size_2();
        String result3 = decoder.upper_price_limit_2();
        String result4 = decoder.tick_size_3();
        String result5 = decoder.upper_price_limit_3();
        String result6 = decoder.tick_size_4();
        String result7 = decoder.upper_price_limit_4();
        String result8 = decoder.tick_size_5();
        String result9 = decoder.upper_price_limit_5();
        String result10 = decoder.tick_size_6();
        String result11 = decoder.upper_price_limit_6();
        String result12 = decoder.tick_size_7();
        String result13 = decoder.upper_price_limit_7();
        String result14 = decoder.tick_size_8();
        String result15 = decoder.upper_price_limit_8();
        String result16 = decoder.tick_size_9();
        String result17 = decoder.upper_price_limit_9();
        String result18 = decoder.tick_size_10();
        String result19 = decoder.upper_price_limit_10();
        String result20 = decoder.tick_size_11();
        String result21 = decoder.upper_price_limit_11();
        String result22 = decoder.tick_size_12();
        String result23 = decoder.upper_price_limit_12();
        String result24 = decoder.tick_size_13();
        String result25 = decoder.upper_price_limit_13();
        String result26 = decoder.tick_size_14();
        String result27 = decoder.upper_price_limit_14();
        String result28 = decoder.tick_size_15();
        String result29 = decoder.upper_price_limit_15();
        String result30 = decoder.tick_size_16();
        String result31 = decoder.upper_price_limit_16();
        String result32 = decoder.tick_size_17();
        String result33 = decoder.upper_price_limit_17();
        String result34 = decoder.tick_size_18();
        String result35 = decoder.upper_price_limit_18();
        String result36 = decoder.tick_size_19();
        String result37 = decoder.upper_price_limit_19();
        String result38 = decoder.tick_size_20();
        String result39 = decoder.upper_price_limit_20();
        String result40 = decoder.tick_size_band();
        return new TickSizeTable(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9, result10, result11, result12, result13, result14, result15, result16, result17, result18, result19, result20, result21, result22, result23, result24, result25, result26, result27, result28, result29, result30, result31, result32, result33, result34, result35, result36, result37, result38, result39, result40);
    }
}

