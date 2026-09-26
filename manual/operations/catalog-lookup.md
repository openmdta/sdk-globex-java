# catalog-lookup

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send exact catalog dataset and parametersJson containing either expression:string or dimensions:object mapping dimension names to string arrays, plus optional cursor:string and limit:number (default 25). Supply a nonempty dimension selection or an expression, never both. Each dimension must be configured by the Catalog and has at most 256 alternatives. Result keys: catalog, dataset_record_type (nullable), dimensions:string[], entries:[{key,dimensions}], next_cursor (nullable). Use returned keys as exact dataset-record references; opaque cursors belong to the original query.

## Request: CatalogLookupQuery

Format: `schemaId=102, templateId=11, version=9, blockLength=0`. The enclosing XML supports version 19; this message emits version 9.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| catalog | data | varDataEncoding | — | — | — |
| parametersJson | data | varDataEncoding | — | — | — |

## Response: CatalogLookupResponse

Format: `schemaId=102, templateId=106, version=9, blockLength=0`. The enclosing XML supports version 19; this message emits version 9.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| resultJson | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
