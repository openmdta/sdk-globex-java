# Environment

| Setting | Value |
| --- | --- |
| Environment | globex |
| Gateway | gateway |
| Applied revision | 7b93bc66bb50f28ca9f0bcd5c05e475e3d28335295c61e484c0238e186e4ad08 |
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
| [firds](datasets/0.md) | globex/firds | catalog |
| [globex](datasets/1.md) | globex/globex | catalog, search |
| [lus](datasets/2.md) | globex/lus | catalog, latest, timeseries |
| [sim](datasets/3.md) | globex/sim | catalog, latest, timeseries |
| [xetra](datasets/4.md) | globex/xetra | catalog |

## Application services

- [observation](services/0.md): `538909d1-7f16-4139-bf73-62c3775f046e`
