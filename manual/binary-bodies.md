# Typed binary bodies and HTTP JSON projections

# Public Gateway messages carry typed SBE bodies, including complete owner frames
inside the Gateway envelope. Names and case are wire names. HTTP remains a
JSON projection; its JSON Schemas describe the same service commands.

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

## Stream metadata

StreamMetadataResponse (schema 102, template 104, version 24) has a one-byte
presence bitmap: bit 0 activity, bit 1 schedule, bit 2 holiday calendar. Three
groups follow: weekly windows (weekday 0 Monday through 6 Sunday, then UTF-8
open/close), exceptions (closed flag, then UTF-8 date/open/close), and holidays
(name-presence flag, then UTF-8 date/name). Trailing UTF-8 members are dataset,
quality, time zone, calendar name, and calendar display name. Empty strings do
not replace the presence flags. The complete response is limited to 128 KiB.

## Catalog search

CatalogSearchQuery (schema 102, template 10, version 27) has two variable-data
members. `searchQuerySbe` is a complete CatalogSearchQuery owner frame
(schema 7, template 5, version 5); `expression` is an optional UTF-8 gateway
identity/list expression, empty when absent. The owner frame contains catalog,
typed filters, ranges, facets, keys, and pagination controls. Expression and
keys cannot both be present. HTTPS still accepts its declared JSON request and
adapts it to the same typed query.

The TypeScript SDK accepts a parameters object. All members are optional: `text` (string, default
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

CatalogSearchResponse (schema 102, template 105, version 26) carries one
`searchPageSbe` variable-data member: a complete CatalogSearchPage frame
(schema 7, template 6, version 7) defined in `schemas/catalog-protocol.xml`.
State 0 is the disabled page and has only `{state:"disabled"}` in the SDK.
Ready and building pages decode to these typed members:

| Member | Type and meaning |
| --- | --- |
| fields | Array of field-description objects supplied by the search projection |
| state | String describing projection availability |
| entries | Array of `{key:string,text:string[],facets:object<string,string>,numbers:object<string,number>}` |
| total | `bigint` or null; unknown is not zero |
| facets | Object of facet -> value -> `bigint` count |
| facet_meta | Object of facet -> `{exhaustive:boolean}` |
| next_cursor | String or null; echo unchanged with the same query |
| indexed_at_millis | Unix milliseconds as `bigint`, or null |
| checkpoint | Catalog WAL cursor or null |
| indexed_records, processed_records | Unsigned `bigint` counters |
| config | `{exposure_approved,text,facets,ranges,max_facet_values,boost,ranking,refresh_seconds}` |
| error | String or null |
| elapsed_ms | Numeric elapsed milliseconds |

Config text/boost selectors are `{field:string,member:string}` where member is
a JSON pointer. Boost may be null. Facet/range selectors also have `name` and
`precedence:string[]`. Ranking is either `{mode:"ordered",factors:[...]}` with
match/class/boost, or `{mode:"weighted",relevance:number,class:number,boost:number}`.
A WAL cursor is `{dataset,generation,incarnation_id,universe_fingerprint,wal_id,max_sequence}`;
incarnation_id is string or null, dataset is string, and the other values are
unsigned 64-bit `bigint` values in the TypeScript SDK.

## Catalog lookup

CatalogLookupQuery (schema 102, template 11, version 23) has a ten-byte fixed
block: `mode:uint8` (0 dimensions, 1 expression), `presence:uint8` (bit 0
cursor, bit 1 limit), and `pageLimit:uint64` (zero when absent). A dimension group
follows. Each dimension contains a group of UTF-8 values and then a UTF-8
name. The final UTF-8 members are catalog, expression, and cursor in that
order. Empty expression or cursor bytes are valid only when their mode or
presence bit specifies them. Supply nonempty configured dimensions or an
expression, never both; each dimension has at most 256 alternatives and an
expression is at most 4096 bytes. An absent limit defaults to 25.

CatalogLookupResponse (schema 102, template 106, version 25) contains one
`browsePageSbe` variable-data member. Its bytes are a complete
CatalogBrowsePage frame (schema 7, template 4, version 6) as defined in
`schemas/catalog-protocol.xml`. The embedded lifecycle is a complete
CatalogLifecycle frame (schema 14, template 8); decode it using the acting
version in its own SBE header. Value origins are typed entries in the browse
page. The generated TypeScript SDK decodes the page to:

| Member | Type |
| --- | --- |
| catalog | Exact dataset string |
| dataset_record_type | String or null |
| dimensions | String[] of dimension names |
| generation | `bigint` unsigned 64-bit integer |
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
(boolean), `effective_time_micros` (`bigint`), `hide_at_micros` and
`hidden_at_unix_seconds` (`bigint` or null), `source_position` (source
position `{incarnation:bigint,message_id:bigint}`), `requirements` (number[][]), `origin_ids` (uint32[]), and typed `origins`. Preserve
opaque source positions if you do not interpret provenance. Record absence,
listing, activity and field permission are separate facts.

## Timeseries pages

The WebSocket TimeseriesPageRequest is typed SBE (schema 102, template 14,
version 30): `resolutionMicros:uint64`, `boundary:uint64`,
`guard:uint64`, `pageLimit:uint32`, `presence:uint8`, `order:uint8`, and
`adjustment:uint8`, followed by length-prefixed UTF-8 selector, dataset,
quality, and cursor, then a `selectedFieldsSbe` variable-data member containing
the complete `FieldSelection` frame (schema 102, template 27, version 30–32).
Its repeating `semantic` members identify business fields, without a 64-field
limit. An empty group selects all fields. Presence bit 0 marks boundary and bit 1 marks guard;
absent numeric members are zero. Order is 0 ascending or 1 descending;
adjustment is 0 raw or 1 split. Empty dataset, quality, and cursor members
mean absent. HTTPS page routes use query parameters, including `limit` and
optional comma-separated `blocks` names; unsigned 64-bit values remain exact
decimal strings. Choose exactly one boundary or cursor. The guard is the other end of the search
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

ServiceCallRequest (schema 102, template 13, version 31) carries a typed
deadline and service ID, command, fingerprint, mutation ID, and `inputSbe`
members. `inputSbe` is a complete frame from the service's pinned owner XML;
its template must match the command manifest. ServiceCallResult (template 109,
version 31) carries typed outcome/error metadata and a complete owner output
or declared-error frame. The internal ServiceCall RPC uses the same owner frame
and typed envelope (schema 11, templates 1 and 2). The generated SDK checks
the fingerprint and frame identity, decodes the owner value, and exposes the
command's declared TypeScript type. Bundled JSON Schemas describe the HTTPS
projection of those same commands. Read commands have no mutation ID. Never
infer retry safety from an HTTP/WebSocket timeout: follow the command's effect
and replay metadata. The gateway caps dispatch deadlines at 30 seconds from
receipt. Owner input is at most 64 KiB and results at most 4 MiB; the effective
transport limit also includes the enclosing session frame. A deadline can
expire after dispatch and therefore produce an unknown mutation outcome.

Keyfigure contracts in the schema inventory define configured fields and their
types. CatalogKeyfiguresRequest (schema 102, template 8, version 28) carries
`priceCutoffMs:uint64?` and `after:uint64`, then catalog, action, key,
contract fingerprint, age mode, expression, and a complete
KeyfiguresSearchQuery owner SBE frame (schema 10, template 3, version 1 or 2).
The owner frame is required for search and empty for other actions. Expression
is resolved by the gateway into search keys and cannot accompany explicit keys.
CatalogKeyfiguresResult (schema 102, template 103, version 32) contains one
complete Keyfigures owner frame in `resultSbe`; no owner frame exceeds 1 MiB.
The public actions are schema, instrument, and search. Schema and instrument
answer with one result: owner template 2 or 4 in schema 10, version 2. A search
answers with zero or more KeyfiguresRowChunk results (template 10) and then
its KeyfiguresSearchResult summary (template 5), followed by session DONE.
Each chunk's `firstRow` is the index of its first row, so chunks must arrive
contiguously from 0; the summary's `rowCount` is the total and must equal the
rows received. Reject a missing, repeated or reordered chunk, a chunk before a
non-search result, and any result after the final frame. Rows are complete
template 6 frames carrying exactly the contract's declared fields in column-ID
order: kind 1 string, 2 integer, 3 number, 4 boolean, or 5 string list (a
`multiple: true` field, values in `listValues`). Rows also carry exact
observation IDs, template 7 provenance frames and a template 8 license frame.
The SDK checks every field against the bundled customer contract fingerprint
before exposing the generated business types. HTTPS returns the reassembled
JSON projection: epoch, observation, provenance and search cutoff times are
decimal strings, and every JSON number is exact in JavaScript. Template 9 is an
internal-only diagnostic result for retained owner actions; the Gateway does
not forward it to public clients.
