package com.openmdta.sdk.p_globex;

/** Typed source-wide Stream delivery. Every callback runs synchronously on the connection. */
@FunctionalInterface
public interface StreamListener {
    /** One borrowed source message, with generated optional getters and change/clear flags. */
    void onUpdate(MarketDataUpdate update) throws Exception;
    /** The subscription's initial message-ID fence; precedes all live updates. */
    default void onFence(long throughMessageId) throws Exception {}
    /** Open message-ID interval (after, through], requiring recovery. */
    default void onGap(long afterMessageId, long throughMessageId) throws Exception {}
    /** Complete message-ID coverage (after, through], distinct from durable sink progress. */
    default void onWatermark(long afterMessageId, long throughMessageId) throws Exception {}
    /** Snapshot coverage boundary; precedes snapshot gaps and records. */
    default void onSnapshotBegin(long throughMessageId) throws Exception {}
    /** A missing message-ID interval recorded in the snapshot header. */
    default void onSnapshotGap(long afterMessageId, long throughMessageId) throws Exception {}
    /** All snapshot records have been delivered; runs before request completion. */
    default void onSnapshotComplete() throws Exception {}
    /**
     * The connection was re-established and this subscription restarts on the new session: a new
     * fence follows and coverage derived from the previous fence must be discarded. Only live
     * subscriptions are replayed; recovery and snapshot requests fail when their session is lost.
     */
    default void onReplay() throws Exception {}
}
