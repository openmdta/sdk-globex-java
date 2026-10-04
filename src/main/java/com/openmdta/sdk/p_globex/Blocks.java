package com.openmdta.sdk.p_globex;

 public final class Blocks {private Blocks() {} public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.AskOhlcDecoder> ASK_OHLC = new Block<>("openmdta::AskOhlc", "AskOhlc", new Format(41957, 18427, 0, 52), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.AskOhlcDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder> BID_ASK = new Block<>("openmdta::BidAsk", "BidAsk", new Format(41957, 7387, 0, 27), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidAskCandleDecoder> BID_ASK_CANDLE = new Block<>("openmdta::BidAskCandle", "BidAskCandle", new Format(41957, 3324, 0, 89), java.util.Set.of("TS_CANDLE", "TS_CANDLE_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidAskCandleDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.BidOhlcDecoder> BID_OHLC = new Block<>("openmdta::BidOhlc", "BidOhlc", new Format(41957, 9015, 0, 52), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.BidOhlcDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.TradeDecoder> TRADE = new Block<>("openmdta::Trade", "Trade", new Format(41957, 3822, 0, 18), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.TradeDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvDecoder> TRADE_OHLCVV = new Block<>("openmdta::TradeOhlcvv", "TradeOhlcvv", new Format(41957, 51484, 0, 61), java.util.Set.of("SNAPSHOT", "STREAM", "TS_RAW", "TS_RAW_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvDecoder::new);

public static final Block<com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvCandleDecoder> TRADE_OHLCVV_CANDLE = new Block<>("openmdta::TradeOhlcvvCandle", "TradeOhlcvvCandle", new Format(41957, 49382, 0, 61), java.util.Set.of("TS_CANDLE", "TS_CANDLE_STREAM"), com.openmdta.sdk.p_globex.sbe.stream_0.TradeOhlcvvCandleDecoder::new);

public static final java.util.List<Block<?>> ALL = java.util.List.of(ASK_OHLC, BID_ASK, BID_ASK_CANDLE, BID_OHLC, TRADE, TRADE_OHLCVV, TRADE_OHLCVV_CANDLE);}
