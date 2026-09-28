# stream-metadata

Finite; wait for DONE. Carry the request in a session OpenRequest, and route responses by request ID. See [session](../session.md) and [encoding](../encoding.md).

Send exact dataset and quality as UTF-8 data. StreamMetadataResponse is typed SBE version 24: presence bits mark activity, schedule and calendar; weekly windows, exceptions and holidays are groups; dataset, quality, time zone and calendar names are trailing UTF-8 members. Metadata is bounded to 128 KiB and describes activity rather than prices. A closed exception and an absent holiday name have explicit flags. Validate the structure before using schedules in UI/state.

## Request: StreamMetadataQuery

Format: `schemaId=102, templateId=9, version=13, blockLength=0`. The enclosing XML supports version 29; this message emits version 13.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |

## Response: StreamMetadataResponse

Format: `schemaId=102, templateId=104, version=24, blockLength=1`. The enclosing XML supports version 29; this message emits version 24.

| Member | Kind | Type / dimensions | Fixed offset | Since version | Null / constant |
| --- | --- | --- | --- | --- | --- |
| presence | field | uint8 | 0 | — | — |
| weeklyWindows | group | largeGroupSizeEncoding | — | — | — |
| weeklyWindows.weekday | field | uint8 | 0 | — | — |
| weeklyWindows.open | data | varDataEncoding | — | — | — |
| weeklyWindows.close | data | varDataEncoding | — | — | — |
| exceptions | group | largeGroupSizeEncoding | — | — | — |
| exceptions.closed | field | uint8 | 0 | — | — |
| exceptions.date | data | varDataEncoding | — | — | — |
| exceptions.open | data | varDataEncoding | — | — | — |
| exceptions.close | data | varDataEncoding | — | — | — |
| holidays | group | largeGroupSizeEncoding | — | — | — |
| holidays.hasName | field | uint8 | 0 | — | — |
| holidays.date | data | varDataEncoding | — | — | — |
| holidays.name | data | varDataEncoding | — | — | — |
| dataset | data | varDataEncoding | — | — | — |
| quality | data | varDataEncoding | — | — | — |
| timeZone | data | varDataEncoding | — | — | — |
| calendarName | data | varDataEncoding | — | — | — |
| calendarDisplayName | data | varDataEncoding | — | — | — |

[Complete gateway XML](../schemas/gateway-protocol.xml) · [JSON envelopes](../json-bodies.md). Variable members follow the acting fixed block in the listed order. Group children repeat per entry.
