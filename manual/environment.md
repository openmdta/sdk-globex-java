# Environment

| Setting | Value |
| --- | --- |
| Environment | globex |
| Gateway | gateway |
| Applied revision | dabcaf4aa60952aedbbcae4899ac7517b1b0dae78bfe50c82a9f768b02f111e7 |
| Public base URL | https://globex.openmdta.com/explorer |
| WebSocket URL | wss://globex.openmdta.com/api/v1/ws |
| Subprotocol | openmdta.sbe-session.v2 |
| Token audience | openmdta-data:globex |
| Runtime behavior version | 1 |

## Limits

| Limit | Value |
| --- | ---: |
| activeRequests | 131072 |
| applicationBodyBytes | 16777216 |
| authTimeoutMillis | 5000 |
| connectionQueueBytes | 67108864 |
| heartbeatIntervalMillis | 5000 |
| heartbeatTimeoutMillis | 15000 |
| maximumGatewaySchemaVersion | 33 |
| processQueueBytes | 268435456 |
| responseBatchMessages | 64 |
| responseBatchTargetBytes | 262144 |
| responseCreditOverheadBytes | 128 |
| responseCredits | 32 |
| responseQueueBytes | 33554432 |
| responseWindowProtocolVersion | 2 |
| serviceInputBytes | 65536 |
| serviceResultBytes | 4194304 |
| websocketMessageBytes | 5242880 |
| writeTimeoutMillis | 15000 |

HTTP SSE enabled: **false**. The current HTTP [OpenAPI document](https://globex.openmdta.com/api/v1/openapi.json) and [JSON Schema](https://globex.openmdta.com/api/v1/schema.json) are served by this gateway; see [HTTP reads](http.md).


## Datasets

| Alias | Exact dataset ID | Configured capabilities |
| --- | --- | --- |
| [globex](datasets/0.md) | globex/globex | catalog, search |
| [lus](datasets/1.md) | globex/lus | catalog, latest, timeseries |
| [xetra](datasets/2.md) | globex/xetra | catalog |

## Application services

- [observation](services/0.md): `35ce1347-4974-4b3b-9d23-1ef8c20cb660`
