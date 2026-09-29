# snapshot

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Select fields by semantic name in the FieldSelection SBE owner frame. `expression` accepts typed identifiers such as `ISIN(US0378331005)`, `US(AAPL)`, `LIST(CODE)`, or an exact `RAW(namespace/name,recordKey)`. Venue groups append `@XNAS,XXXX` for a union or `@XNAS>XXXX` for fallback. A RAW selector has no venue preferences. Set adjustment to `raw` or `split`; send the exact dataset explicitly. Snapshot batches have phase 1; live updates phase 2. A stream first establishes current state, then updates it. Preserve clear markers and source message boundaries.

## Request: SnapshotRequest

Format: `schemaId=102, templateId=1, version=30, blockLength=0`. The enclosing XML supports version 32; this message emits version 30.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| expression | data | varDataEncoding | — | — | — |
| adjustment | data | varDataEncoding | — | 11 | — |
| dataset | data | varDataEncoding | — | 14 | — |
| selectedFieldsSbe | data | varDataEncoding | — | 30 | — |

## Response: MarketDataMessageBatch

Format: `schemaId=102, templateId=108, version=12, blockLength=1`. The enclosing XML supports version 32; this message emits version 12.

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

[Complete gateway XML](../schemas/gateway-protocol.xml) · [Binary bodies](../binary-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
