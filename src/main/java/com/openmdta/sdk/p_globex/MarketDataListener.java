package com.openmdta.sdk.p_globex;

/** Synchronous callbacks. Each update is borrowed until onUpdate returns. */
@FunctionalInterface
public interface MarketDataListener {
    void onUpdate(MarketDataUpdate update) throws Exception;
    /** Event-time history gap, not a Stream message-ID recovery interval. -1 means unknown. */
    default void onGap(long fromEventTimeMicros, long throughEventTimeMicros) throws Exception {}
    /** The connection was re-established; the request restarts and delivers a fresh snapshot after this call. */
    default void onReplay() throws Exception {}
}
