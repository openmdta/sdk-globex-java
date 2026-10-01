# ts-raw-stream

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Use the same selector and FieldSelection semantic names as snapshot. Historical `from` and `through` are Unix microseconds with `from <= through`; quality is explicitly RT, DL or EOD as configured. Raw requests carry `maxRows`; send 1..10000. Candle reads have a server cap of 10000 rows. Raw windows are at most 15 minutes; candle windows at most five days, cadence at least 1 second and at most 7200 intervals. Candle-stream updateIntervalMillis controls partial-bar refresh. Historical gaps describe missing event-time coverage; they are distinct from message-ID feed recovery gaps. Send adjustment `raw` or `split` and exact dataset. Finite reads require DONE; streaming variants continue after their initial history.

## Request: TsRawStreamRequest

Format: `schemaId=102, templateId=6, version=30, blockLength=20`. The enclosing XML supports version 33; this message emits version 30.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| from | field | uint64 | 0 | — | — |
| through | field | uint64 | 8 | — | — |
| maxRows | field | uint32 | 16 | — | — |
| expression | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| adjustment | data | varDataEncoding | — | 11 | — |
| dataset | data | varDataEncoding | — | 14 | — |
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
