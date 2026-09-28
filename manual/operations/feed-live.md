# feed-live

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send the exact dataset, configured quality, and selected-field block mask. Feed recovery additionally sends exclusive afterMessageId and inclusive throughMessageId. Live must start with a fence; snapshot must start with its header. FeedControl kinds are 1=fence, 2=opened gap, 3=coverage watermark. Follow the full [persistence and recovery algorithm](../persistence-and-recovery.md). Message batches alone never establish durable coverage.

## Request: FeedLiveRequest

Format: `schemaId=102, templateId=15, version=29, blockLength=8`. The enclosing XML supports version 29; this message emits version 29.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| blockMask | field | uint64 | 0 | — | — |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| selectedFieldsSbe | data | varDataEncoding | — | 29 | — |

## Response: FeedControl

Format: `schemaId=102, templateId=111, version=18, blockLength=17`. The enclosing XML supports version 29; this message emits version 18.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| kind | field | uint8 | 0 | — | — |
| afterMessageId | field | uint64 | 1 | — | — |
| throughMessageId | field | uint64 | 9 | — | — |
| dataset | data | varDataEncoding | — | — | — |

## Response: MarketDataMessageBatch

Format: `schemaId=102, templateId=108, version=12, blockLength=1`. The enclosing XML supports version 29; this message emits version 12.

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

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
