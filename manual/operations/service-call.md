# service-call

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Set the logical service ID, command and exact command fingerprint from the service page. deadlineUnixMillis is an absolute Unix-millisecond deadline. inputSbe is a complete owner frame matching the command's input template in its pinned manifest. Read commands send empty mutationId; mutations require a stable persisted nonempty ID (at most 128 bytes). ServiceCallResult carries typed outcome/error metadata and a complete owner output or declared-error frame. HTTPS projects those same values to JSON. Follow the owner SBE schema and retain outcome/mutation ID when reporting errors. Transport failure after sending a mutation means its outcome can be unknown.

## Request: ServiceCallRequest

Format: `schemaId=102, templateId=13, version=31, blockLength=8`. The enclosing XML supports version 32; this message emits version 31.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| deadlineUnixMillis | field | uint64 | 0 | — | — |
| serviceId | data | varDataEncoding | — | — | — |
| command | data | varDataEncoding | — | — | — |
| contractFingerprint | data | varDataEncoding | — | — | — |
| mutationId | data | varDataEncoding | — | — | — |
| inputSbe | data | varDataEncoding | — | — | — |

## Response: ServiceCallResult

Format: `schemaId=102, templateId=109, version=31, blockLength=2`. The enclosing XML supports version 32; this message emits version 31.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| ok | field | uint8 | 0 | — | — |
| outcome | field | uint8 | 1 | — | — |
| serviceId | data | varDataEncoding | — | — | — |
| command | data | varDataEncoding | — | — | — |
| contractFingerprint | data | varDataEncoding | — | — | — |
| mutationId | data | varDataEncoding | — | — | — |
| errorCode | data | varDataEncoding | — | — | — |
| errorMessage | data | varDataEncoding | — | — | — |
| valueSbe | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [Binary bodies](../binary-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
