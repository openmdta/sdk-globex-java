# Persistence and recovery

The following message-ID algorithm applies to Stream feeds. Catalog feeds use
the separate [Catalog storage algorithm](#catalog-feed-storage) below.

Store exact uint64 message IDs and event microseconds, dataset, quality, record
key, owner field identity, schema/version, payload or clear marker. Preserve
whole source messages: selected fields from one message belong to the same
atomic update. Keep the raw bytes if you need to re-decode after a schema change.

For a latest-value store, key rows by `(dataset, quality, recordKey, field)` and
retain the message ID per field. Apply a value/clear only if its message ID is
newer than that field's stored ID. A later event timestamp is not a substitute
for a later message ID. Clears must retain their ID so older recovery data
cannot resurrect a deleted value.

## Whole-feed protocol

`FeedLiveRequest` starts with `FeedControl(kind=1)` as the atomic subscription
fence, even on a quiet stream. Kind 2 opens a missing interval
`(afterMessageId, throughMessageId]`. Kind 3 is a complete-coverage watermark.
Subsequent batches preserve source message boundaries. The largest message ID
you happened to receive is **not** proof that every preceding selected message
has arrived. Advance the durable covered head on coverage controls, not data.

`FeedRecoveryRequest` reads an exclusive/inclusive interval. Validate every
returned message ID against that interval. Complete it only when the request
finishes successfully; ERROR, disconnect and cancellation leave the gap open.
Retrying recovery can duplicate messages. Deduplicate by source message ID and
apply the per-field monotonic rule. IDs need not be contiguous after filtering.
Do not infer an unreported gap just because successive selected IDs differ.

`FeedSnapshotRequest` starts with `FeedSnapshotHeader`, which supplies the source
head incorporated in the snapshot plus unresolved gaps. The remaining batches
contain latest values. A snapshot is not a complete historical message log.

## Latest bootstrap algorithm

1. Load durable state: covered head, unresolved intervals and an initialized bit.
2. Start live delivery and the latest snapshot concurrently. Require a live
   fence before treating any live batch as connected coverage.
3. Apply live and snapshot updates using message IDs **per field**, so a stale
   snapshot cannot overwrite newer live data. Keep writes serialized or use
   transactional compare-and-update.
4. Persist each snapshot gap, and the interval `(snapshotHead, liveFence]` when
   the fence is newer than the snapshot. Recover those intervals.
5. After the snapshot and all bootstrap recoveries complete, commit initialized
   state. If the process stops before that commit, repeat bootstrap on restart.

For an initialized store, reconnect live, obtain its new fence, and persist a
gap `(durableHead, newFence]` if needed. Recover this and all previously open
gaps while accepting new live updates. Persist the gap before advancing the
head over it. A watermark can advance the head while explicitly recorded gaps
remain outstanding. Keep these two facts separately.

## Transaction boundary

Commit message rows, clears, gap-open/gap-close changes and the durable head in
one storage transaction. A successful gap close must be atomic with its final
data writes. Acknowledge/replenish credits only after the commit if durability
is required. Storage failure stops delivery; do not advance an in-memory cursor
and report success. The wire protocol does not create exactly-once writes in
your database: idempotent transactions and persisted cursors provide that.

Message-log mode skips latest bootstrap. On a first connection with no prior
cursor it begins at the live fence; it does not promise all historical data.
Retained-history gaps may require operator policy (wait/retry, rebuild from a
snapshot, or report incomplete history). Never silently mark a failed recovery
as covered. Changing dataset, quality, block selection, owner schema identity or
environment requires a new cursor namespace or an explicit migration.

## Catalog feed storage

`CatalogFeedRequest` follows the whole selected Catalog. Send up to 64 field
labels (empty selects all), the exact dataset and the last durable cursor, or
an empty string when no state exists. Keep the cursor as an opaque UTF-8 string
of at most 4096 bytes. Scope it by environment, dataset, field selection and
credential permissions. It is not a Stream message ID or an event timestamp.

The server sends typed `CatalogFeedControl` and `CatalogRecord` SBE bodies on the same
request, in order. The control kind is a `uint8` followed by one UTF-8 cursor
member (empty only for kind 1):

1. Kind 1, snapshot begin: discard any unfinished staging table and start
   a new empty one. Keep the previously committed Catalog visible until the
   replacement is ready. This can occur on first connection, an invalidated
   generation/incarnation/schema, excessive replay lag, or retained-WAL loss.
2. Apply both phase-1 snapshot and phase-2 update records to staging while a
   snapshot is open. Rows are keyed by exact record key within this Catalog.
   `exists=true` replaces the selected record state, including lifecycle and
   its complete selected field set; `exists=false` removes it. Do not keep old
   selected fields that are absent from a replacement record. Permissions can
   make a record absent even when the underlying source has it.
3. Kind 2, snapshot complete with cursor: atomically replace the
   visible table with staging and persist this cursor. Require an open snapshot.
4. Outside a snapshot, apply update records to the visible table. On
   kind 3 cursor control, commit all preceding records and the
   new cursor together. Require no open snapshot. Repeated records are safe
   because each is a replacement/deletion, not a numeric delta.

After disconnect, discard incomplete staging and reconnect with the last
committed cursor. Never persist a cursor ahead of its rows. A valid retained
cursor can resume with updates directly; the server may instead start another
snapshot. A malformed cursor is an error, not a request for an empty snapshot.
Cancellation or ERROR does not complete a snapshot. A cursor can advance without
visible records after filtering, so do not require a row before committing it.
Return response credits after durable writes. On storage failure stop the
subscription and retain the previous committed cursor.
