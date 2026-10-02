package com.openmdta.sdk.p_globex;

/** Records are borrowed; snapshot/cursor callbacks are ordered with this subscription's records. */
@FunctionalInterface
public interface CatalogListener {
    void onRecord(CatalogRecord record) throws Exception;
    default void onSnapshotBegin() throws Exception {}
    default void onSnapshotComplete(String cursor) throws Exception {}
    default void onCursor(String cursor) throws Exception {}
    /** The connection was re-established and a Catalog read restarts; Catalog subscriptions instead fail and are resumed from their cursor. */
    default void onReplay() throws Exception {}
}
