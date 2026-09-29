# listing-latest

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

ListingLatestRequest is typed SBE: a group of 1..64 uint16 field IDs, then UTF-8 dataset, quality, and exact record key. Quality is RT/DL/EOD; key is at most 1024 bytes. ListingLatestEvent is typed SBE: snapshot and connected bytes; source field-ID group; block group with uint16 ID, uint64 message ID and event microseconds, clear byte, nested qualified-license clause group, and native payload; then source dataset, quality, key and incarnation. License clause rows sharing an index form an OR; distinct indexes form an AND. An empty namespace/license row is Public. Payload includes the native universe 4-byte blockLength/version prefix. Treat incarnation/disconnect changes as a reset of subscription continuity. Since version 22, an optional trailing length-prefixed aggregationQuality payload contains uint32 trading day, uint8 partial (0/1), and uint64 last applied ID. Partial remains sticky until the next trading day; quality-only events can contain no blocks.

## Request: ListingLatestRequest

Format: `schemaId=102, templateId=12, version=21, blockLength=0`. The enclosing XML supports version 32; this message emits version 21.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| blocks | group | largeGroupSizeEncoding | — | — | — |
| blocks.id | field | uint16 | 0 | — | — |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| key | data | varDataEncoding | — | — | — |

## Response: ListingLatestEvent

Format: `schemaId=102, templateId=107, version=22, blockLength=2`. The enclosing XML supports version 32; this message emits version 22.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| snapshot | field | uint8 | 0 | — | — |
| connected | field | uint8 | 1 | — | — |
| sourceBlocks | group | largeGroupSizeEncoding | — | — | — |
| sourceBlocks.id | field | uint16 | 0 | — | — |
| blocks | group | largeGroupSizeEncoding | — | — | — |
| blocks.id | field | uint16 | 0 | — | — |
| blocks.messageId | field | uint64 | 2 | — | — |
| blocks.eventTimeMicros | field | uint64 | 10 | — | — |
| blocks.clear | field | uint8 | 18 | — | — |
| blocks.requirements | group | largeGroupSizeEncoding | — | — | — |
| blocks.requirements.clauseIndex | field | uint16 | 0 | — | — |
| blocks.requirements.namespace | data | varDataEncoding | — | — | — |
| blocks.requirements.license | data | varDataEncoding | — | — | — |
| blocks.payload | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| key | data | varDataEncoding | — | — | — |
| incarnation | data | varDataEncoding | — | — | — |
| aggregationQuality | data | varDataEncoding | — | 22 | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [Binary bodies](../binary-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
