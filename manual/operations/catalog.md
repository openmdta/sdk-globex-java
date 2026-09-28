# catalog

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Encode the identifiers group, then the field-label group, then the exact catalog dataset. Up to 256 identifiers and 64 fields are accepted. Each identifier/field is UTF-8 variable data in an entry whose fixed block length is zero. Field labels come from the owner Catalog contract. Interpret exists/lifecycle and field absence independently. Each CatalogRecord supplies record identity and selected owner payloads; wait for DONE to finish the result set.

## Request: CatalogRequest

Format: `schemaId=102, templateId=5, version=5, blockLength=0`. The enclosing XML supports version 27; this message emits version 5.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| identifiers | group | largeGroupSizeEncoding | — | — | — |
| identifiers.identifier | data | varDataEncoding | — | — | — |
| fields | group | largeGroupSizeEncoding | — | — | — |
| fields.label | data | varDataEncoding | — | — | — |
| catalog | data | varDataEncoding | — | — | — |

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
