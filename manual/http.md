# HTTP reads and events

Use the public base URL and SSE setting in [environment.md](environment.md).
The same delegated token used for the binary session authenticates data requests:
`Authorization: Bearer <unpadded-base64url-MDToken>`. URL-encode query parameters
and individual path segments. GET has no JSON body; structured lookup, search and
service reads use POST with `Content-Type: application/json`.

## Discover this gateway's HTTP contract

`GET /api/v1/openapi.json` returns the configured OpenAPI 3.1 document, and
`GET /api/v1/schema.json` returns its JSON Schema, owner block definitions,
dataset aliases/capabilities and service metadata. Save both when implementing
the HTTP projection: JSON property names/types are defined there and can differ
from native SBE fields. These documents are assembled by this gateway from its
selected owner contracts and configuration, not from a universal API model.
Compare `x-openmdta-contract-sha256` when refreshing them. A changed digest calls
for revalidation of cached models. The complete SBE path in this bundle works
without retrieving these HTTP documents.

`GET /api/v1/datasets` returns `{datasets:[...],sse:boolean}`. Each dataset has
`alias`, exact `dataset`, `qualities`, `capabilities`, `catalog`, `latest`,
`timeseries` and current `available`. Use configured aliases from the environment
page; an available source still requires permission for each selected field.

## Current read routes

Only use routes present in this gateway's OpenAPI document. All paths below
start with `/api/v1`.

| Read | Method and path | Input |
| --- | --- | --- |
| Latest values | GET `/market-data/latest` | selector, optional dataset, blocks, adjustment |
| Raw history | GET `/market-data/timeseries/raw` | same selection plus from, through, optional quality and maxRows |
| Candle history | GET `/market-data/timeseries/candles` | same history selection plus cadenceMicros |
| History page | GET `/market-data/timeseries/{raw,candles}/page` | selector, exactly one boundary or cursor, optional guard, order, limit; candles require cadenceMicros |
| Dataset-pinned prices | GET `/datasets/{alias}/{latest,timeseries/raw,timeseries/candles}` | same parameters; alias fixes the dataset |
| Catalog records | GET `/datasets/{alias}/records` | one selector, optional comma-separated fields; omit fields for all |
| Catalog lookup/search | POST `/datasets/{alias}/{lookup,search}` | the corresponding JSON parameters from [binary bodies](binary-bodies.md) |
| Stream metadata | GET `/streams/{dataset}/{quality}/metadata` | exact dataset and configured quality in the path |
| Key figures | GET `/keyfigures/{catalog}/{schema,instrument}`, POST `/keyfigures/{catalog}/search` | instrument selector/query as specified in OpenAPI. Any authenticated client may search; no dataset grant is required for now. |
| Application service read | POST `/services/{alias}/{command}` | the command's declared input object; mutations use the binary service-call operation |

`selector` uses the same typed identifier expressions as binary reads. `blocks`
contains comma-separated block names from the HTTP schema, for example `BidAsk`.
Omitted selection
chooses supported fields. Historical times, cadence, boundary and guard are
decimal strings in Unix microseconds (cadence is a duration); IDs remain exact
decimal strings in JSON. `adjustment` defaults to raw. `maxRows` is 1..10000;
page `limit` is 1..200 and `order` is asc/desc. Preserve page cursors verbatim with
unchanged query semantics. URI length is bounded to 8192 bytes; finite execution
has a 30-second deadline and an 8 MiB response limit. Use pages or narrower reads
when a result would exceed those bounds.

Finite prices return `{batches:[...]}`, and pages return `{batches:[...],page:...}`.
Each batch has `phase` (SNAPSHOT/UPDATE), `dataset`, `datasetRecordKey`,
`messages:[{messageId,fields:[{eventTimeMicros,clear,value}]}]` and `gaps` with
nullable decimal-string `fromEventTimeMicros`/`throughEventTimeMicros`. The owner
block's `type` identifies the value schema. Preserve source message boundaries,
clear markers and gap uncertainty just as for binary batches.

Catalog reads return `{records:[...]}`. Records carry phase, dataset, selector,
datasetRecordKey, exists, nullable lifecycle, decoded fields and rawFields.
Each raw field has label, optional subfield, wireId, nullable fixedLength and
`payloadBase64` (standard Base64, not base64url). Decoded fields may be absent
when a codec is unavailable; use the raw payload and bundled owner SBE XML.

## SSE subscriptions

SSE exists only when enabled in environment.md and advertised by OpenAPI. Append
`/events` to the advertised latest/history subscription route and use the same
query parameters. Read `text/event-stream` incrementally; concatenate a complete
event's `data:` lines before JSON parsing. Events are named `snapshot` or `update`
and contain the batch shape above. Ignore keepalive comments. An `error` event
contains `{error:{code,message}}` and terminates the subscription.

Use a streaming HTTP client that sends the Authorization header. Disconnection,
expiry and revocation end delivery. Reconnect with refreshed credentials and
start a new snapshot/history subscription. SSE has no durable replay cursor or
Last-Event-ID recovery; use the binary feed protocol for durable coverage and
gap repair. Cancel/close the response when the consumer stops.

Data responses are `Cache-Control: no-store`. Errors normally have
`{error:{code,message}}`; check status and content type before parsing. Preserve
401/403 authorization failures, 429 rate limiting and Retry-After, and temporary
backend failures separately. Retry only reads whose semantics permit it.

## Legacy read routes

Resolve the paths below against the public base URL in [environment.md](environment.md).
These are GET requests with URL-encoded query parameters, not JSON request
bodies. Send `Authorization: Bearer <unpadded-base64url-MDToken>` for authenticated
reads. Use the binary session for feed subscriptions and recovery.

| Path | Availability | Parameters / result |
| --- | --- | --- |
| `/api/v1/snapshot` | A configured Latest source | `selector` required; optional `quality`, `adjustment` (raw/split, default raw), `source_field`. |
| `/api/v1/timeseries` | A configured Timeseries source | `selector`, `from`, `through` required; optional `quality`, `resolution` (microseconds, default 0 for raw), `max_rows` (default 10000), `adjustment`. All numeric query values are decimal. |
| `/api/v1/catalog/search` | A configured search catalog | `dataset` required, optional `text`, `expression`, `limit`, `cursor`, and `action` (search/autocomplete/search-describe). `filters`, `ranges`, `facets` are JSON text in query parameters. Any authenticated client may search; no dataset grant is required for now. |

The Explorer inventory endpoint `/api/v1/markets` requires an administrator
session; use this bundle's environment inventory for a standalone data client.

Use `RAW(EXACT_DATASET,recordKey)` to pin a source where identity resolution is
unwanted. Use only datasets/capabilities and qualities listed in this bundle.
Snapshot returns `{datasetRecordKey:string,dataset:string,blocks:object[],sourceBlocks:object[]}`.
Each source block is `{field:string,time:string,messageId:string,data:object}`;
time/messageId preserve uint64 as strings. `source_field` requests a semantic
source field. Ordinary blocks are the owner codec's JSON representation, not
SBE bytes. Their owner-specific structure can differ; the SBE operation and
bundled owner XML provide the complete portable binary contract.

Timeseries returns `{datasetRecordKey:string,dataset:string,blocks:object[],gaps:object[],status:number}`.
Gap bounds are `from_event_time_micros` and `through_event_time_micros` (uint64
or null); preserve uncertainty. Historical window/row/resolution restrictions
match the corresponding finite binary read. Prefer the binary timeseries-page
operation when implementing cursor pagination.

Successful authenticated reads use `Cache-Control: no-store`. Check status and
content type before parsing: authentication errors return JSON, while some
backend/validation errors return text. 401 indicates invalid/missing credentials,
403 insufficient permission, 429 rate limiting (respect Retry-After when present),
and 503 temporary unavailability. Refresh an expired token before retrying.
Avoid caching authenticated data under a key that omits credential scope.
