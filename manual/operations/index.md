# Operations

These operations are configured for this environment. Availability and permission are checked for each request. Each page gives the exact format descriptor and headerless body layout.

| Operation | Capability | Delivery |
| --- | --- | --- |
| [snapshot](snapshot.md) | latest | Finite; wait for DONE |
| [stream](stream.md) | latest | Continues until cancelled/error |
| [ts-raw](ts-raw.md) | timeseries | Finite; wait for DONE |
| [catalog](catalog.md) | catalog | Finite; wait for DONE |
| [ts-raw-stream](ts-raw-stream.md) | timeseries | Continues until cancelled/error |
| [stream-metadata](stream-metadata.md) | latest | Finite; wait for DONE |
| [catalog-search](catalog-search.md) | search | Finite; wait for DONE |
| [catalog-lookup](catalog-lookup.md) | catalog | Finite; wait for DONE |
| [listing-latest](listing-latest.md) | latest | Continues until cancelled/error |
| [service-call](service-call.md) | service | Finite; wait for DONE |
| [timeseries-page](timeseries-page.md) | timeseries | Finite; wait for DONE |
| [feed-live](feed-live.md) | feed | Continues until cancelled/error |
| [feed-recovery](feed-recovery.md) | feed | Finite; wait for DONE |
| [feed-snapshot](feed-snapshot.md) | feed | Finite; wait for DONE |
| [catalog-feed](catalog-feed.md) | catalog | Continues until cancelled/error |
