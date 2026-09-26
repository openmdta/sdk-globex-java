# stream-metadata

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send exact dataset and quality as UTF-8 data. Decode metadataJson using the bundled stream-metadata JSON Schema. Metadata is bounded to 128 KiB and describes activity and coverage, not a stream of prices. Validate its structure before using schedules or coverage in UI/state.

## Request: StreamMetadataQuery

Format: `schemaId=102, templateId=9, version=13, blockLength=0`. The enclosing XML supports version 19; this message emits version 13.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |

## Response: StreamMetadataResponse

Format: `schemaId=102, templateId=104, version=13, blockLength=0`. The enclosing XML supports version 19; this message emits version 13.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| metadataJson | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
