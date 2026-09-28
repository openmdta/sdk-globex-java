# catalog-lookup

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send exact catalog dataset and typed CatalogLookupQuery fields. The fixed block is mode:uint8 (0 dimensions, 1 expression), presence:uint8 (bit 0 cursor, bit 1 limit), and pageLimit:uint64 (zero when absent). Then encode dimension entries, each with a values group and UTF-8 name, followed by UTF-8 catalog, expression, and cursor. Supply a nonempty dimension selection or an expression, never both. Each dimension has at most 256 alternatives. An absent limit defaults to 25. CatalogLookupResponse version 25 carries a complete CatalogBrowsePage owner frame (schema 7, template 4, version 6), with typed lifecycle, field inventory and page metadata. Use returned keys as exact dataset-record references; opaque cursors belong to the original query.

## Request: CatalogLookupQuery

Format: `schemaId=102, templateId=11, version=23, blockLength=10`. The enclosing XML supports version 27; this message emits version 23.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| mode | field | uint8 | 0 | — | — |
| presence | field | uint8 | 1 | — | — |
| pageLimit | field | uint64 | 2 | — | — |
| dimensions | group | largeGroupSizeEncoding | — | — | — |
| dimensions.values | group | largeGroupSizeEncoding | — | — | — |
| dimensions.values.value | data | varDataEncoding | — | — | — |
| dimensions.name | data | varDataEncoding | — | — | — |
| catalog | data | varDataEncoding | — | — | — |
| expression | data | varDataEncoding | — | — | — |
| cursor | data | varDataEncoding | — | — | — |

## Response: CatalogLookupResponse

Format: `schemaId=102, templateId=106, version=25, blockLength=0`. The enclosing XML supports version 27; this message emits version 25.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| browsePageSbe | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
