# Schema inventory

All XML files are complete and include-free. Use XML for wire types and their lexical values, and the layout JSON for resolved token offsets and declaration trees. Gateway operations use schema 102; session framing uses schema 5; token envelopes use schema 8. Other protocol schemas describe native nested payloads and internal encoding conventions, not additional public endpoints.

| File | Schema ID | Schema version | Resolved layout |
| --- | ---: | ---: | --- |
| [catalog-firds.xml](catalog-firds.xml) | 40132 | 0 | [JSON](catalog-firds.layout.json) |
| [catalog-globex.xml](catalog-globex.xml) | 49499 | 0 | [JSON](catalog-globex.layout.json) |
| [catalog-lus.xml](catalog-lus.xml) | 41874 | 0 | [JSON](catalog-lus.layout.json) |
| [catalog-openmdta.xml](catalog-openmdta.xml) | 41957 | 0 | [JSON](catalog-openmdta.layout.json) |
| [catalog-protocol.xml](catalog-protocol.xml) | 7 | 9 | [JSON](catalog-protocol.layout.json) |
| [catalog-sim.xml](catalog-sim.xml) | 31312 | 0 | [JSON](catalog-sim.layout.json) |
| [catalog-xetra.xml](catalog-xetra.xml) | 3154 | 0 | [JSON](catalog-xetra.layout.json) |
| [gateway-protocol.xml](gateway-protocol.xml) | 102 | 33 | [JSON](gateway-protocol.layout.json) |
| [keyfigures-protocol.xml](keyfigures-protocol.xml) | 10 | 2 | [JSON](keyfigures-protocol.layout.json) |
| [mdtoken.xml](mdtoken.xml) | 8 | 3 | [JSON](mdtoken.layout.json) |
| [sbe-websocket.xml](sbe-websocket.xml) | 5 | 1 | [JSON](sbe-websocket.layout.json) |
| [service-protocol.xml](service-protocol.xml) | 11 | 1 | [JSON](service-protocol.layout.json) |
| [stream-0.xml](stream-0.xml) | 41957 | 0 | [JSON](stream-0.layout.json) |
| [stream-1.xml](stream-1.xml) | 41957 | 0 | [JSON](stream-1.layout.json) |
| [stream-2.xml](stream-2.xml) | 41957 | 0 | [JSON](stream-2.layout.json) |
| [stream-3.xml](stream-3.xml) | 41957 | 0 | [JSON](stream-3.layout.json) |
| [stream-4.xml](stream-4.xml) | 41957 | 0 | [JSON](stream-4.layout.json) |
| [stream-5.xml](stream-5.xml) | 41957 | 0 | [JSON](stream-5.layout.json) |
| [stream-6.xml](stream-6.xml) | 41957 | 0 | [JSON](stream-6.layout.json) |
| [stream-7.xml](stream-7.xml) | 41957 | 0 | [JSON](stream-7.layout.json) |
| [stream-8.xml](stream-8.xml) | 41957 | 0 | [JSON](stream-8.layout.json) |
| [stream-protocol.xml](stream-protocol.xml) | 1 | 18 | [JSON](stream-protocol.layout.json) |
| [timeseries-protocol.xml](timeseries-protocol.xml) | 6 | 5 | [JSON](timeseries-protocol.layout.json) |

## JSON bodies

- [catalog-lookup-request.json](catalog-lookup-request.json)
- [listing-request.json](listing-request.json)
- [service-result.json](service-result.json)
- [stream-metadata.json](stream-metadata.json)
- [timeseries-page-request.json](timeseries-page-request.json)
