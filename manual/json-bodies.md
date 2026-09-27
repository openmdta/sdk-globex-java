# Remaining JSON bodies and typed SBE replacements

Only Stream metadata, Catalog search/lookup, Keyfigures, and application service
calls still carry JSON in SBE variable-data members. Those bodies are UTF-8 in
the member named by the operation's XML; do not add an extra JSON length inside
that member. The Listing and Timeseries sections below describe typed SBE
replacements. Names and case are wire names. Omitted optional JSON request
members use server defaults; preserve explicit `null` values in JSON responses.
Readers should tolerate additional JSON response properties. Bundled JSON
Schemas specify the remaining service and metadata request types.

## Listing events

ListingLatestRequest (schema 102, template 12, version 21) contains a repeating
group of 1..64 `id:uint16` field IDs, then length-prefixed UTF-8 dataset,
quality, and exact record key. ListingLatestEvent (template 107, version 21)
starts with `snapshot:uint8` and `connected:uint8`, a source field-ID group,
then a block group. Each block contains `id:uint16`, `messageId:uint64`,
`eventTimeMicros:uint64`, `clear:uint8`, a nested qualified-license requirement
group, and the native field payload. Requirement rows with the same
`clauseIndex:uint16` are alternatives; consecutive clause indexes are ANDed.
Each row has UTF-8 namespace and license; both empty means Public. Source
dataset, quality, key, and incarnation follow the groups. These qualified
names differ from Catalog's dataset-local numeric license IDs. Empty blocks
can signal connectivity/incarnation changes. Do not discard them.

## Catalog search

Parameters are an object. All members are optional: `text` (string, default
empty), `filters` (object of field -> string or string[]), `ranges` (array),
`facets` (string[]), `keys` (string[] or null), `limit` (integer, zero chooses
the server default), `autocomplete` and `describe` (booleans, default false),
`cursor` and `expected_incarnation` (strings or null). `expression` (string)
asks the gateway to resolve keys; omit `keys` when using it. The outer request's
catalog is authoritative. Unknown parameters are rejected.

Each range is `{field,gt?,ge?,lt?,le?}` with finite numeric bounds. `min` aliases
`ge`; `max` aliases `le`. Do not combine gt/ge or lt/le. A closed equal-bound
interval requires ge/le. `absolute_margin` and `relative_margin` must remain
zero for catalog search. Ask for `describe:true` before choosing configured
text/facet/range fields.

The result object has these members:

| Member | Type and meaning |
| --- | --- |
| fields | Array of field-description objects supplied by the search projection |
| state | String describing projection availability |
| entries | Array of `{key:string,text:string[],facets:object<string,string>,numbers:object<string,number>}` |
| total | Integer or null; unknown is not zero |
| facets | Object of facet -> value -> integer count |
| facet_meta | Object of facet -> `{exhaustive:boolean}` |
| next_cursor | String or null; echo unchanged with the same query |
| indexed_at_millis | Unix milliseconds or null |
| checkpoint | Catalog WAL cursor or null |
| indexed_records, processed_records | Unsigned integer counters |
| config | `{exposure_approved,text,facets,ranges,max_facet_values,boost,ranking,refresh_seconds}` |
| error | String or null |
| elapsed_ms | Numeric elapsed milliseconds |

Config text/boost selectors are `{field:string,member:string}` where member is
a JSON pointer. Boost may be null. Facet/range selectors also have `name` and
`precedence:string[]`. Ranking is either `{mode:"ordered",factors:[...]}` with
match/class/boost, or `{mode:"weighted",relevance:number,class:number,boost:number}`.
A WAL cursor is `{dataset,generation,incarnation_id,universe_fingerprint,wal_id,max_sequence}`;
incarnation_id is string or null, dataset is string, and the other values are
unsigned 64-bit JSON integers. Use a lossless JSON reader when retaining them.

## Catalog lookup

Parameters are `{dimensions?,expression?,cursor?,limit?}`. Dimensions maps names
to nonempty string arrays. Supply at least one dimension configured by the
Catalog, with at most 256 alternatives per dimension. Expressions are at most
4096 bytes. Choose dimensions or expression, never both. Limit defaults
to 25. The result is:

| Member | Type |
| --- | --- |
| catalog | Exact dataset string |
| dataset_record_type | String or null |
| dimensions | String[] of dimension names |
| generation | Unsigned 64-bit integer |
| incarnation | Opaque string |
| entries | Array of `{key:string,dimensions:object<string,string[]>,lifecycle:object|null,requirements:number[][]}` |
| fields | Array of `{entity_type:string|null,multiple:boolean,label:string,semantic:string|null,wire_id:uint16,fixed_length:uint32|null}` |
| stream_fields | Array of `{semantic:string,id:uint16,compatible_growth:boolean,fixed_length:uint32|null}` |
| stream_fields_error | String or null |
| next_cursor | String or null |
| scanned | Unsigned integer |

Catalog requirements are an AND of arrays, each an OR of dataset-local license
IDs; 16383 denotes public. Lifecycle contains `listing` ("Listed" or
"NotListed"), `activity` ("Active", "Inactive", "Unknown", or null), `visible`
(boolean), `effective_time_micros` (uint64), `hide_at_micros` and
`hidden_at_unix_seconds` (uint64 or null), `source_position` (source
position `{incarnation:uint64,message_id:uint64}`), `requirements` (number[][]), and `origin_ids` (uint32[]). Preserve
opaque source positions if you do not interpret provenance. Record absence,
listing, activity and field permission are separate facts.

## Timeseries pages

The WebSocket TimeseriesPageRequest is typed SBE (schema 102, template 14,
version 20): `blockMask:uint64`, `resolutionMicros:uint64`, `boundary:uint64`,
`guard:uint64`, `pageLimit:uint32`, `presence:uint8`, `order:uint8`, and
`adjustment:uint8`, followed by length-prefixed UTF-8 selector, dataset,
quality, and cursor. Presence bit 0 marks boundary and bit 1 marks guard;
absent numeric members are zero. Order is 0 ascending or 1 descending;
adjustment is 0 raw or 1 split. Empty dataset, quality, and cursor members
mean absent. The HTTPS projection still uses
`schemas/timeseries-page-request.json` with `limit` as its JSON property and
decimal strings for unsigned
64-bit values. Choose exactly one boundary or cursor. The guard is the other end of the search
interval; it must be on the appropriate side of the boundary. Raw guards span
at most 30 days; candle guards at most 365 days plus one resolution. Omitted
guards choose a bounded server window (at least seven days). Candle resolution
must appear in Stream metadata and is at least one second.

Consume binary MarketDataMessageBatch responses first, followed by a typed
TimeseriesPageResult SBE footer: `fromMicros:uint64`, `throughMicros:uint64`,
`status:uint8`, and `nextCursor:UTF-8` (empty means none), then session DONE.
A missing next cursor means this guarded scan is finished, not that all
possible history exists. Status 0 is exact, 1 open, 2 partial, 3 not ready; retain explicit
gaps. Echo a cursor unchanged with the same selector/dataset/quality/mask/
resolution/order/adjustment. A nonempty cursor is not proof of durable coverage.

## Service results and keyfigures

Service result envelopes and command input/output/error definitions are bundled
JSON Schemas. Read commands have no mutation ID. Never infer retry safety from
an HTTP/WebSocket timeout: follow the command's effect and replay metadata.
The gateway caps dispatch deadlines at 30 seconds from receipt. Input JSON is
at most 64 KiB and results at most 4 MiB; the effective transport limit also
includes the enclosing session frame. A deadline can expire after dispatch and
therefore produce an unknown mutation outcome.

Keyfigure contracts in the schema inventory define configured fields and their
types. The operation carries JSON results; use the action/contract fingerprint
specified by that contract. Unknown fields can be retained as JSON. This data
may contain uint64 JSON numbers as well as explicitly string-encoded integers;
use a lossless JSON parser when exact identities or cursors matter.
