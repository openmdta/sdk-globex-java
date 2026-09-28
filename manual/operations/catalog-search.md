# catalog-search

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

CatalogSearchQuery version 27 carries a complete owner CatalogSearchQuery frame (schema 7, template 5, version 5) followed by an optional UTF-8 gateway expression. The owner frame contains exact catalog dataset, text, typed filters, numeric ranges, facets, optional keys, cursor, limit, autocomplete and describe controls. Do not combine keys with expression. Use describe=true to discover configured search fields/operators before selecting filters. CatalogSearchResponse version 26 carries a complete owner CatalogSearchPage frame (schema 7, template 6, version 7). Its typed result contains fields, state, entries, facets, facet metadata, optional total/cursor/time, checkpoint and configuration. Keep uint64 counters exact. Search requires the dataset SEARCH grant; search visibility does not confer record-detail licenses.

## Request: CatalogSearchQuery

Format: `schemaId=102, templateId=10, version=27, blockLength=0`. The enclosing XML supports version 27; this message emits version 27.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| searchQuerySbe | data | varDataEncoding | — | — | — |
| expression | data | varDataEncoding | — | — | — |

## Response: CatalogSearchResponse

Format: `schemaId=102, templateId=105, version=26, blockLength=0`. The enclosing XML supports version 27; this message emits version 26.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| searchPageSbe | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
