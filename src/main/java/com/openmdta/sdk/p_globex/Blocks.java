package com.openmdta.sdk.p_globex;

 public final class Blocks {private Blocks() {} public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.AskDailyOhlcDecoder> ASK_DAILY_OHLC = new Block<>("openmdta::AskDailyOhlc", new Format(100, 15, 3, 53), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.AskDailyOhlcDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder> BID_ASK = new Block<>("openmdta::BidAsk", new Format(100, 10, 3, 27), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_1.BidAskCandleDecoder> BID_ASK_CANDLE = new Block<>("openmdta::BidAskCandle", new Format(100, 12, 3, 89), java.util.Set.of("TS_CANDLE", "TS_CANDLE_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_1.BidAskCandleDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidDailyOhlcDecoder> BID_DAILY_OHLC = new Block<>("openmdta::BidDailyOhlc", new Format(100, 14, 3, 53), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidDailyOhlcDecoder::new);

public static final java.util.List<Block<?>> ALL = java.util.List.of(ASK_DAILY_OHLC, BID_ASK, BID_ASK_CANDLE, BID_DAILY_OHLC);}
