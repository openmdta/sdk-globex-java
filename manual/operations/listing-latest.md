# listing-latest

Continues until cancelled/error. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

The parameters JSON object is {dataset:string,quality:string,key:string,blocks:number[]}. Quality is RT/DL/EOD; key is a nonempty exact record key up to 1024 bytes; blocks contains 1..64 deployed field IDs. Response event JSON contains source (same selector), incarnation:string, snapshot:boolean, connected:boolean, and blocks:[{id,messageId:string,eventUs:string,clear:boolean,requirements,payload:number[]}]. The payload is a native universe body including its 4-byte blockLength/version prefix. uint64 values use decimal strings. Treat incarnation/disconnect changes as a reset of subscription continuity.

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
