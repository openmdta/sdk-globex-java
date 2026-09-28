# catalog-feed

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Subscribe to the complete selected Catalog using up to 64 field labels, exact catalog dataset and an opaque cursor (empty for a fresh snapshot). An empty fields group requests all fields; do not send `*`. CatalogFeedControl uses a typed kind byte (1 snapshot begin, 2 snapshot complete, 3 cursor) and one UTF-8 cursor member; only snapshot begin has an empty cursor. CatalogRecord responses share the catalog read layout, with phase 1 for snapshot and 2 for updates. Stage snapshot rows, replace durable state only at snapshot_complete, and commit later records before their cursor. A new snapshot_begin discards incomplete staging. See [Catalog persistence](../persistence-and-recovery.md#catalog-feed-storage).

## Request: CatalogFeedRequest

Format: `schemaId=102, templateId=18, version=19, blockLength=0`. The enclosing XML supports version 27; this message emits version 19.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.label | data | varDataEncoding | — | — | — |
| catalog | data | varDataEncoding | — | — | — |
| cursor | data | varDataEncoding | — | — | — |

## Response: CatalogFeedControl

Format: `schemaId=102, templateId=113, version=19, blockLength=1`. The enclosing XML supports version 27; this message emits version 19.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| kind | field | uint8 | 0 | — | — |
| cursor | data | varDataEncoding | — | — | — |

## Response: CatalogRecord

Format: `schemaId=102, templateId=102, version=5, blockLength=30`. The enclosing XML supports version 27; this message emits version 5.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| phase | field | Phase | 0 | — | — |
| exists | field | uint8 | 1 | — | — |
| lifecyclePresent | field | uint8 | 2 | — | — |
| listing | field | uint8 | 3 | — | — |
| activity | field | uint8 | 4 | — | — |
| visible | field | uint8 | 5 | — | — |
| effectiveTimeMicros | field | uint64 | 6 | — | — |
| hideAtMicros | field | uint64 | 14 | — | 18446744073709551615 |
| hiddenAtUnixSeconds | field | uint64 | 22 | — | 18446744073709551615 |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.wireId | field | uint16 | 0 | — | — |
| fields.fixedLength | field | uint32 | 2 | — | 4294967295 |
| fields.label | data | varDataEncoding | — | — | — |
| fields.subfield | data | varDataEncoding | — | — | — |
| fields.payload | data | varDataEncoding | — | — | — |
| catalog | data | varDataEncoding | — | — | — |
| identifier | data | varDataEncoding | — | — | — |
| recordIdentifier | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
