package com.openmdta.sdk.p_globex;

 public final class Blocks {private Blocks() {} public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.AskOhlcDecoder> ASK_OHLC = new Block<>("openmdta::AskOhlc", new Format(100, 18, 4, 52), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.AskOhlcDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder> BID_ASK = new Block<>("openmdta::BidAsk", new Format(100, 10, 4, 27), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_1.BidAskCandleDecoder> BID_ASK_CANDLE = new Block<>("openmdta::BidAskCandle", new Format(100, 12, 4, 89), java.util.Set.of("TS_CANDLE", "TS_CANDLE_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_1.BidAskCandleDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidOhlcDecoder> BID_OHLC = new Block<>("openmdta::BidOhlc", new Format(100, 17, 4, 52), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidOhlcDecoder::new);

public static final java.util.List<Block<?>> ALL = java.util.List.of(ASK_OHLC, BID_ASK, BID_ASK_CANDLE, BID_OHLC);}
