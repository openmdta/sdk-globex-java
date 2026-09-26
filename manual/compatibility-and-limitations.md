# Compatibility and implementation checks

This bundle describes the environment/revision in environment.md. Permissions,
source availability and retained history are runtime facts and can change.
Configured capabilities do not guarantee a particular record exists or that
your credential may read it. Check errors and lifecycle states.

Use each operation's emitted format version; the schema's maximum supported
version is not necessarily the version emitted by every message. Preserve
unknown fields when storing raw data. Only skip added fixed fields when acting
block lengths/version rules make that safe. Unknown mandatory templates,
incompatible types, changed schema IDs and unsupported variable sections need
an explicit compatibility decision, not a guessed decoder.

Current rough edges are visible in this contract: there are three prefix forms
(full SBE header, session format plus headerless body, Catalog's 4-byte prefix),
two group-count widths, per-message emitted versions, JSON nested inside SBE,
and separate semantic field/universe/template identities. Treat these as
distinct encodings. The schemas alone cannot specify persistence, permissions
or mutation retry semantics; the accompanying chapters are required.

The bundled CatalogRecord XML corrects an older descriptor declaration that
listed eight bytes and a separate version. Deployed codecs use six bytes and
the version in the native payload prefix. This correction changes the schema
description, not the transmitted bytes.

The operation index covers the generated WebSocket client surface. [HTTP reads](http.md)
describe public read routes, schema discovery and optional SSE. Internal service
schemas are included to explain nested native payloads, not to advertise internal
service endpoints. Resolve public endpoints against the URL in environment.md.

## SDK terminology

`streamFeed(blocks, sink)` and `catalogFeed(fields, sink)` are sink-based
workflows, with `streamFeedMemory` and `catalogFeedMemory` for process-local
storage. Catalog and Stream have separate commands and sinks; their delivery
has no special ordering relationship.

The low-level Stream methods map to the existing wire commands:

| Public method | Wire operation | Result |
| --- | --- | --- |
| `streamSubscribe(blocks)` | `feed-live` | Live request handle with data and coverage controls |
| `streamRecover(start, end, blocks)` | `feed-recovery` | Recovery request handle for the exclusive-start, inclusive-end range |
| `streamSnapshot(blocks)` | `feed-snapshot` | Snapshot pointer, gaps and message iterator |

The future raw Catalog method name is `catalogSubscribe(fields, options?)`.
The existing `catalog-feed` wire command emits records, snapshot controls and
opaque-cursor controls in one request. It resumes from a cursor and can resnapshot
when retained WAL history is unavailable. Separate `catalogRecover` and
`catalogSnapshot` commands do not exist. These API names do not rename SBE
templates or change the layouts in this bundle.

## Implementation checks

Before using a new implementation:

- Decode the provided byte examples and reproduce request bytes exactly.
- Test zero-length data, empty groups, null versus zero, maximum uint64, negative
  decimals, non-ASCII text, clear markers, truncated buffers and invalid lengths.
- Reject wrong audience, expired credentials, invalid MAC and wrong origin.
- Exercise concurrent request IDs, credits, empty DONE, ERROR and cancellation.
- Disconnect during snapshot/recovery, replay duplicate messages, apply a stale
  snapshot after live data, and crash between data and cursor writes.
- Retry service mutations with the same persisted ID and distinguish an unknown
  outcome from a confirmed not-applied result.

`contract.json` is the sanitized machine-readable inventory. It contains no
credentials or private service addresses. Reproducible generation excludes
wall-clock timestamps; artifact digests and the environment revision identify
the inputs.
