# catalog-search

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send exact catalog dataset and parametersJson. Parameters: optional text:string, expression:string, filters:object mapping field to string[], ranges:array of {field,gt?,ge?,lt?,le?}, facets:string[], cursor:string, limit:number, autocomplete:boolean, describe:boolean. Use describe=true to discover configured search fields/operators before selecting filters. Pass returned next_cursor unchanged for continuation. Result keys: fields, state, entries (key,text[],facets,numbers), facets, facet_meta (exhaustive per field), total, next_cursor, indexed_at_millis and config. Nullable total/cursor/time remain null. Search requires the dataset SEARCH grant; search visibility does not confer record-detail licenses.

## Request: CatalogSearchQuery

Format: `schemaId=102, templateId=10, version=8, blockLength=0`. The enclosing XML supports version 19; this message emits version 8.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| catalog | data | varDataEncoding | — | — | — |
| parametersJson | data | varDataEncoding | — | — | — |

## Response: CatalogSearchResponse

Format: `schemaId=102, templateId=105, version=8, blockLength=0`. The enclosing XML supports version 19; this message emits version 8.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| resultJson | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
