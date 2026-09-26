# JSON carried inside SBE

JSON bodies are UTF-8 in the variable-data member named in the operation's XML.
Do not add an extra JSON length inside that member. Names and case below are
wire names. Omitted optional request members use server defaults; preserve
explicit `null` values in responses. Readers should tolerate additional response
properties. Bundled JSON Schemas specify the service, metadata, listing and page
request types; this chapter supplies the remaining envelopes and conventions.

## Listing events

`ListingLatestEvent.eventJson` is:

```json
{
  "source": {"dataset":"TEST@feed","quality":"RT","key":"AAPL","blocks":[7]},
  "incarnation":"opaque source incarnation",
  "snapshot":true,
  "connected":true,
  "blocks":[{
    "id":7,"messageId":"18446744073709551614","eventUs":"1790000000000000",
    "clear":false,"requirements":{"clauses":["Public"]},"payload":[0,0,0,0]
  }]
}
```

The payload above is illustrative; real payloads follow their owner schema.
An alternative requirement clause is `{"AnyOf":[{"namespace":"IEX","license":"TOPS"}]}`.
Every clause must be satisfied; any license within one AnyOf is sufficient.
These qualified names differ from Catalog's dataset-local numeric license IDs.
Empty blocks can signal connectivity/incarnation changes. Do not discard them.

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

## Catalog feed

Whole-Catalog feed controls have exactly three wire variants:
`{"kind":"snapshot_begin"}`, `{"kind":"snapshot_complete","cursor":"..."}`,
and `{"kind":"cursor","cursor":"..."}`. The cursor is opaque text, including
any embedded JSON, and is returned unchanged. The SDK's local reset event and
camelCase control names are not wire messages. Follow the
[storage algorithm](persistence-and-recovery.md#catalog-feed-storage).

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

The request schema is in `schemas/timeseries-page-request.json`. Send unsigned
64-bit values as decimal strings. `order` is asc/desc and `limit` is 1..200.
Choose exactly one boundary or cursor. The guard is the other end of the search
interval; it must be on the appropriate side of the boundary. Raw guards span
at most 30 days; candle guards at most 365 days plus one resolution. Omitted
guards choose a bounded server window (at least seven days). Candle resolution
must appear in Stream metadata and is at least one second.

Consume binary MarketDataMessageBatch responses first, followed by
`{from:string,through:string,nextCursor:string|null,status:number}` and session
DONE. A null nextCursor means this guarded scan is finished, not that all
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
