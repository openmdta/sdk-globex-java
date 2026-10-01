# timeseries-page

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

TimeseriesPageRequest version 30 is typed SBE: resolutionMicros:uint64, boundary:uint64, guard:uint64, limit:uint32, presence:uint8 (bit 0 boundary present, bit 1 guard present), order:uint8 (0 asc, 1 desc), adjustment:uint8 (0 raw, 1 split), then UTF-8 selector, dataset, quality and cursor members and a FieldSelection SBE frame containing semantic field names. An empty selection requests all permitted fields. Choose exactly one boundary or cursor. Absent numeric members are zero and distinguished by presence; absent text members are empty. Cursor text is opaque and must be returned with the same selector/dataset/field selection/resolution/quality/order/adjustment. First decode MarketDataMessageBatch responses. The final TimeseriesPageResult is typed SBE with fromMicros:uint64, throughMicros:uint64, status:uint8, and an optional UTF-8 nextCursor (empty means none); then the session sends DONE. Keep exact uint64 values and owner payload formats. Do not change query semantics while continuing a cursor.

## Request: TimeseriesPageRequest

Format: `schemaId=102, templateId=14, version=30, blockLength=31`. The enclosing XML supports version 33; this message emits version 30.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| resolutionMicros | field | uint64 | 0 | — | — |
| boundary | field | uint64 | 8 | — | — |
| guard | field | uint64 | 16 | — | — |
| pageLimit | field | uint32 | 24 | — | — |
| presence | field | uint8 | 28 | — | — |
| order | field | uint8 | 29 | — | — |
| adjustment | field | uint8 | 30 | — | — |
| selector | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| cursor | data | varDataEncoding | — | — | — |
| selectedFieldsSbe | data | varDataEncoding | — | 30 | — |

## Response: MarketDataMessageBatch

Format: `schemaId=102, templateId=108, version=33, blockLength=1`. The enclosing XML supports version 33; this message emits version 33.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| phase | field | Phase | 0 | — | — |
| messages | group | largeGroupSizeEncoding | — | — | — |
| messages.messageId | field | uint64 | 0 | — | — |
| messages.firstField | field | uint32 | 8 | — | — |
| messages.fieldCount | field | uint16 | 12 | — | — |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.fieldId | field | uint16 | 0 | — | — |
| fields.version | field | uint16 | 2 | — | — |
| fields.blockLength | field | uint16 | 4 | — | — |
| fields.eventTimeMicros | field | uint64 | 6 | — | — |
| fields.clear | field | uint8 | 14 | — | — |
| fields.payloadOffset | field | uint32 | 15 | — | — |
| fields.payloadLength | field | uint32 | 19 | — | — |
| gaps | group | largeGroupSizeEncoding | — | — | — |
| gaps.fromEventTimeMicros | field | uint64 | 0 | — | 18446744073709551615 |
| gaps.throughEventTimeMicros | field | uint64 | 8 | — | 18446744073709551615 |
| datasetRecordKey | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |
| payload | data | varDataEncoding | — | — | — |

## Response: TimeseriesPageResult

Format: `schemaId=102, templateId=110, version=17, blockLength=17`. The enclosing XML supports version 33; this message emits version 17.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| fromMicros | field | uint64 | 0 | — | — |
| throughMicros | field | uint64 | 8 | — | — |
| status | field | uint8 | 16 | — | — |
| nextCursor | data | varDataEncoding | — | — | — |

## Response: DatasetFields

Format: `schemaId=102, templateId=114, version=33, blockLength=0`. The enclosing XML supports version 33; this message emits version 33.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.fieldId | field | uint16 | 0 | — | — |
| fields.semantic | data | varDataEncoding | — | — | — |
| fields.layout | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [Binary bodies](../binary-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
