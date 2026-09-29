# WebSocket session

Use the WebSocket URL and subprotocol in [environment.md](environment.md).
Send one binary WebSocket message per session SBE frame. Text frames are not
application messages. TLS and WebSocket framing can be handled by a standard
WebSocket library; encode the binary session yourself using
[sbe-websocket.xml](schemas/sbe-websocket.xml).

1. Connect with `Sec-WebSocket-Protocol: openmdta.sbe-session.v2`.
2. Immediately send `AuthRequest` (schema 5, template 1, version 0, fixed block
   8): nonzero `uint64 requestId` (conventionally 1), then length-prefixed opaque
   token bytes. Wait for its successful response before opening requests.
3. Reserve the authentication ID for the connection. The server sends empty
   `CONTINUE` responses on that ID as application heartbeats. Do not dispatch
   them as market data. Close/reconnect after the advertised heartbeat timeout.
4. For each operation allocate a distinct nonzero ID, usually starting at 2.
   Send `OpenRequest` (template 2, block 40): request ID, 16 trace-ID bytes,
   8 parent-span bytes, the 8-byte application format descriptor, and a
   length-prefixed **headerless** application body. All-zero trace/span bytes
   mean no propagated trace. Keep an ID-to-operation table.
5. For a feed, send the bounded response window described below after opening.
   Finite requests can use `CreditRequest` (template 4, block 12): target ID and
   `uint32 credits` in `1..responseCredits`. Replenish only after consuming or
   durably writing a response. Credit messages have no separate request ID.
6. Decode `Response` (template 101, block 17): request ID at offset 0, status at
   8, format at 9, then body and UTF-8 error as separate length-prefixed data.
   Status 1 is `CONTINUE`, 2 is `DONE`, 3 is `ERROR`. Process any application
   body according to its descriptor. `DONE` ends the request; `ERROR` fails it.
   An empty terminal body is normal. Empty format fields accompany no body.
7. To cancel, allocate a fresh ID and send `CancelRequest` (template 3, block
   16): cancellation request ID and target ID. `CancelResponse` (template 102,
   block 17) contains both IDs and a one-byte boolean. `cancelled=false` can
   mean the target already completed. Ignore late target frames after disposal.

Authentication is performed once per connection. Sending AUTH again terminates
the connection. A request ID cannot equal the auth ID or an active request ID.
Use monotonically increasing IDs and reconnect before exhausting uint64.
Malformed session messages can close the connection. Operation errors normally
terminate only that request. Server errors are UTF-8 text and are not a stable
enumeration; structured service errors are described separately.

On disconnect, fail pending reads or retry them according to caller policy.
Restart subscriptions from a fresh snapshot/fence and repair gaps; a socket
reconnect alone does not prove continuity. Back off with jitter (for example
250 ms doubling to 30 s). Do not retry an ambiguous mutation with a new mutation
ID. Refresh expiring credentials by reconnecting with a newly issued token.

The effective input limit is the smaller of the application and WebSocket
limits in environment.md, including session overhead. Bound queues by both
count and bytes; a slow consumer must pause credits, cancel, or disconnect.
WebSocket ping/pong control frames are separate from application heartbeats.

## Bounded response windows

Require the `openmdta.sbe-session.v2` subprotocol in the upgrade response.
Unchanged session templates keep acting version 0, including AUTH, OPEN,
CANCEL and CreditRequest, even when codecs are generated from schema version 1.

For each feed request send exactly one ResponseWindow (schema 5,
template 5, version 1, block 20): target ID (uint64), response count (uint32),
byte allowance (uint32), maximum application body size (uint32). The count
cannot exceed 32, bytes cannot exceed 32 MiB, and maximum body size cannot
exceed 16 MiB or the public WebSocket limit minus framing overhead. Reserve
all requested allowances against the connection's 64 MiB budget before
opening requests, including completed requests whose buffers are still held.
A window must fit at least one maximum body plus the 128-byte credit charge.

Each CONTINUE consumes one count credit and `application body length + 128`
byte credits. After consuming that whole response, send ReleaseResponses
(template 6, version 1, block 16): target ID, released response count, released
bytes. Releasing more than was outstanding is a protocol error. DONE, ERROR,
AUTH heartbeats and cancellation acknowledgments consume no credit.

A CONTINUE can carry ResponseBatch (schema 5, template 103, version 1,
block 0). Its group header is uint16 block length 8 and uint32 count (1..64).
Each entry contains an eight-byte format descriptor followed by a uint32
length and that many body bytes. Preserve entry order; nested ResponseBatch
entries are forbidden. Release the outer response's credit only after every
entry has been consumed. Controls flush preceding records and remain separate
responses. The SDK exposes the original logical messages and sink events.

Credits express buffer capacity, independently of rate limiting. Managed
feeds return them after awaiting sink writes; they never replace an atomic
transaction containing data, gap changes and durable resume state. A slow
sink pauses production while other requests can continue within their own
reserved windows.
