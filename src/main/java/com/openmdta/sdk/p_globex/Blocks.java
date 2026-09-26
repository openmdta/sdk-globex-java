package com.openmdta.sdk.p_globex;

 public final class Blocks {private Blocks() {} public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder> BID_ASK = new Block<>("openmdta::BidAsk", new Format(100, 10, 3, 27), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskCandleDecoder> BID_ASK_CANDLE = new Block<>("openmdta::BidAskCandle", new Format(100, 12, 3, 89), java.util.Set.of("TS_CANDLE", "TS_CANDLE_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskCandleDecoder::new);

public static final java.util.List<Block<?>> ALL = java.util.List.of(BID_ASK, BID_ASK_CANDLE);}
