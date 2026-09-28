# service-call

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Set the logical service ID, command and exact command fingerprint from the service page. deadlineUnixMillis is an absolute Unix-millisecond deadline. inputJson is a JSON object matching its input definition. Read commands send empty mutationId; mutations require a stable persisted nonempty ID (at most 128 bytes). Result JSON is {ok:true,result:<output>} or {ok:false,result:null,error:{code:string,message:string,outcome:"not_applied"|"unknown",details?:<error-specific value>}}. Follow the service schema and retain outcome/mutation ID when reporting errors. Transport failure after sending a mutation means its outcome can be unknown.

## Request: ServiceCallRequest

Format: `schemaId=102, templateId=13, version=16, blockLength=8`. The enclosing XML supports version 29; this message emits version 16.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| deadlineUnixMillis | field | uint64 | 0 | — | — |
| serviceId | data | varDataEncoding | — | — | — |
| command | data | varDataEncoding | — | — | — |
| contractFingerprint | data | varDataEncoding | — | — | — |
| mutationId | data | varDataEncoding | — | — | — |
| inputJson | data | varDataEncoding | — | — | — |

## Response: ServiceCallResult

Format: `schemaId=102, templateId=109, version=16, blockLength=0`. The enclosing XML supports version 29; this message emits version 16.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| resultJson | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
