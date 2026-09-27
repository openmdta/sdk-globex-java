# listing-latest

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

ListingLatestRequest is typed SBE: a group of 1..64 uint16 field IDs, then UTF-8 dataset, quality, and exact record key. Quality is RT/DL/EOD; key is at most 1024 bytes. ListingLatestEvent is typed SBE: snapshot and connected bytes; source field-ID group; block group with uint16 ID, uint64 message ID and event microseconds, clear byte, nested qualified-license clause group, and native payload; then source dataset, quality, key and incarnation. License clause rows sharing an index form an OR; distinct indexes form an AND. An empty namespace/license row is Public. Payload includes the native universe 4-byte blockLength/version prefix. Treat incarnation/disconnect changes as a reset of subscription continuity.

## Request: ListingLatestRequest

Format: `schemaId=102, templateId=12, version=15, blockLength=0`. The enclosing XML supports version 19; this message emits version 15.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| parameters | data | varDataEncoding | — | — | — |

## Response: ListingLatestEvent

Format: `schemaId=102, templateId=107, version=15, blockLength=0`. The enclosing XML supports version 19; this message emits version 15.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| event | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
