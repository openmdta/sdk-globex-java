# timeseries-page

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

parametersJson uses selector:string, optional dataset:string and quality:string, blockMask:decimal-string uint64, resolutionMicros:decimal-string uint64, order:'asc'|'desc', limit:1..200, adjustment:'raw'|'split', and exactly one of boundary:decimal-string microseconds or cursor:string. Optional guard is a decimal-string time bound. Cursor text is opaque and must be returned with the same selector/dataset/mask/resolution/quality/order/adjustment. First decode MarketDataMessageBatch responses. The final TimeseriesPageResult JSON contains from:string, through:string, nextCursor:string|null, and status:number; then the session sends DONE. Keep exact decimal strings and owner payload formats. Do not change query semantics while continuing a cursor.

## Request: TimeseriesPageRequest

Format: `schemaId=102, templateId=14, version=17, blockLength=0`. The enclosing XML supports version 19; this message emits version 17.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| parametersJson | data | varDataEncoding | — | — | — |

## Response: MarketDataMessageBatch

Format: `schemaId=102, templateId=108, version=12, blockLength=1`. The enclosing XML supports version 19; this message emits version 12.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| phase | field | Phase | 0 | — | — |
| messages | group | largeGroupSizeEncoding | — | — | — |
| messages.messageId | field | uint64 | 0 | — | — |
| messages.firstField | field | uint32 | 8 | — | — |
| messages.fieldCount | field | uint16 | 12 | — | — |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.schemaId | field | uint16 | 0 | — | — |
| fields.templateId | field | uint16 | 2 | — | — |
| fields.version | field | uint16 | 4 | — | — |
| fields.blockLength | field | uint16 | 6 | — | — |
| fields.eventTimeMicros | field | uint64 | 8 | — | — |
| fields.clear | field | uint8 | 16 | — | — |
| fields.payloadOffset | field | uint32 | 17 | — | — |
| fields.payloadLength | field | uint32 | 21 | — | — |
| gaps | group | largeGroupSizeEncoding | — | — | — |
| gaps.fromEventTimeMicros | field | uint64 | 0 | — | 18446744073709551615 |
| gaps.throughEventTimeMicros | field | uint64 | 8 | — | 18446744073709551615 |
| datasetRecordKey | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |
| payload | data | varDataEncoding | — | — | — |

## Response: TimeseriesPageResult

Format: `schemaId=102, templateId=110, version=17, blockLength=0`. The enclosing XML supports version 19; this message emits version 17.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| resultJson | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
