# globex — Java 17 client

This package targets applied revision `8bab71590974c802be14b3b591e87f79ffb5109f0d2e96089b73d97069b33de3`. Its endpoints, datasets,
field bindings and service fingerprints describe this one environment. Start
with [the environment](manual/environment.md), [operations](manual/operations/index.md)
and [codec index](codecs.md). Credentials are intentionally absent.

## Install from your Git-hosted SDK with JitPack

Publish this generated Maven project at the root of your SDK repository, then
use [JitPack](https://docs.jitpack.io/). Replace `OWNER`, `REPOSITORY` and
`TAG_OR_COMMIT` with that repository's coordinates; pin an immutable commit or
release tag. These are examples, not a pre-published OpenMDTA artifact.

```xml
<repositories>
  <repository><id>jitpack.io</id><url>https://jitpack.io</url></repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>com.github.OWNER</groupId>
    <artifactId>REPOSITORY</artifactId>
    <version>TAG_OR_COMMIT</version>
  </dependency>
</dependencies>
```

```kotlin
repositories { mavenCentral(); maven("https://jitpack.io") }
dependencies { implementation("com.github.OWNER:REPOSITORY:TAG_OR_COMMIT") }
```

The checked-in `jitpack.yml` selects Java 17 and runs Maven install. JitPack
needs access to the repository; private repositories require its private access
configuration. To build locally, use JDK 17+ and Maven 3.9+:

```sh
mvn -B verify
```

SBE 1.40.2 generates codecs during Maven's `generate-sources` phase. The JAR and
sources JAR include those codecs; clients need only the runtime dependencies,
including Agrona 2.6.1. Run applications with:

```sh
java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED ...
```

Agrona uses this JDK access for its buffers. Maven's `.mvn/jvm.config` supplies it
for builds, but applications must set it in their own JVM launch configuration.

## Instrument selectors and typed reads

`MarketSelector` is immutable. Factories validate expression syntax locally;
the gateway resolves instruments and checks availability and permissions.

```java
var apple = MarketSelector.isin("US0378331005").venue("XNAS");
var union = MarketSelector.us("AAPL").venue("XNAS", "XNYS");
var fallback = MarketSelector.us("AAPL").venueFallback("XNAS", "XNYS");
var mixed = MarketSelector.list("GER40")
    .venueGroups(List.of(List.of("XETR", "XFRA"), List.of("*")));
var exact = MarketSelector.raw("EXACT_DATASET@source", "caseSensitiveRecordKey");
```

`isin`, `us`, `cusip`, `sedol`, `wkn`, `figi`, `list` and `custom(type, value)`
normalize scalar identifiers independently of the JVM locale. `raw` preserves
case and cannot have venue preferences. Venue methods replace previous venue
preferences. List selectors support latest/snapshot streaming, not single-record
history. The String overloads remain available for expressions outside the builder.

```java
import com.openmdta.sdk.p_globex.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

try (var client = Client.connect(tokenBytes).join()) {
    var dataset = client.datasetLus();
    var apple = MarketSelector.isin("US0378331005").venue("XNAS");
    try (var request = dataset.latest(apple, List.of(Blocks.BID_ASK), update -> {
        if (update.isBidAskCleared()) {
            // Delete the stored quote for update.recordKeyBytes().
            return;
        }
        update.bidAsk().ifPresent(quote -> quote.bid().ifPresent(bid -> {
            BigDecimal price = bid.price();
            long size = bid.size();
            // quote, bid and price are immutable and safe to retain.
        }));
    })) {
        request.completion().get(10, TimeUnit.SECONDS);
    }
}
```

Dataset methods are generated from this environment's aliases: `lus` becomes
`client.datasetLus()`, and `us-quotes` becomes `client.datasetUsQuotes()`.
The [codec index](codecs.md) lists the exact available methods. Generation rejects
aliases that produce the same method name. `dataset(String)` remains available
for dynamic selection. Generated methods select `RT` when configured, otherwise
the first configured quality; use `.quality("DL")` to choose explicitly.

`update.bidAsk()` returns an `Optional` containing an immutable record in the
`model` package. Nested optional composites use `Optional`, optional numbers use
`OptionalInt`, `OptionalLong` or `OptionalDouble`, and SBE decimals become exact
`BigDecimal` values (`mantissa × 10^exponent`). Required members are direct values.
Newer fixed fields absent in an older schema version return empty Optionals.
Catalog values work the same way: `record.basicMasterdata().ifPresent(master ->
use(master.name()))`. Repeated groups become immutable lists, strings become owned
strings, and unsigned 64-bit values retain Java `long` bit patterns.

These value accessors allocate on each call, materialize the selected value, and
remain valid after the callback. `getBidAsk()` and `getBasicMasterdata()` retain
their existing borrowed, allocation-free decoder API. For repeated Catalog fields,
`forEachClassificationValue((key, value) -> ...)` supplies owned values; its key
remains borrowed. Value names colliding with metadata methods gain a `Value`
suffix, for example `listingValue()`; Java reserved member names gain `_`.

`latestStream` uses the same typed callback for the initial state and subsequent
updates. `timeseries(selector, from, through, blocks, listener)` uses `Instant`
bounds and the same typed values; a `MarketDataListener` can also implement
`onGap(fromEventTimeMicros, throughEventTimeMicros)` for history coverage gaps.

Each `MarketDataUpdate` is **one source message**, with a record key, dataset,
message ID, snapshot flag and per-block event times. Named getters are generated
from this environment's schemas: `getBidAsk()` returns `Optional<BidAskDecoder>`.
The same getters work in `latest`, `latestStream`, `timeseries`,
`streamSubscribe`, `streamRecover`, `streamSnapshot` and Stream sink callbacks.
All selected fields from a source message arrive together in one callback. A
transport batch may contain many messages; it never turns their fields into
independent updates. Field selection can omit fields, and latest snapshots contain
the surviving latest fields grouped by their original message IDs.

| This message's BidAsk state | `getBidAsk()` | `isBidAskChanged()` | `isBidAskCleared()` |
| --- | --- | --- | --- |
| Unchanged | empty | false | false |
| New value | present | true | false |
| Explicit clear | empty | true | true |

Multiple blocks can be selected together. Accessing an unselected block, through
a getter or a state flag, throws `IllegalArgumentException`; it does not return
empty. Decoders, buffers and Optional containers are allocated at request setup
and reused on delivery. Calling a getter does not allocate or copy the payload.
There is no template-ID dispatch, decoder construction or cast in application code.
Generic consumers can still use `value(Blocks.X)` (typed decoder or null),
`changed(X)`, `cleared(X)` and `eventTimeMicros(X)`.

## Typed Catalog reads and subscriptions

```java
dataset.read(apple, List.of(Fields.OPENMDTA__BASIC_MASTERDATA), record -> {
    record.basicMasterdata().ifPresent(master -> {
        String name = master.name(); // owned, safe to retain
    });
});

var subscription = dataset.catalogSubscribe(
    List.of(Fields.OPENMDTA__BASIC_MASTERDATA), savedCursor, new CatalogListener() {
        @Override public void onSnapshotBegin() {
            // Begin a fresh staging store; abandon any incomplete staging snapshot.
        }
        @Override public void onRecord(CatalogRecord record) {
            if (!record.exists()) {
                // Delete record.recordKeyBytes() from the current target store.
                return;
            }
            // Replace this record in the target store, including absent fields.
            record.basicMasterdata().ifPresent(master -> {
                String name = master.name();
                // Write name to the replacement record.
            });
            // The owned master value is safe to retain; record is borrowed.
        }
        @Override public void onSnapshotComplete(String cursor) {
            // Atomically publish the staging store and persist cursor.
        }
        @Override public void onCursor(String cursor) {
            // Persist after preceding record writes are durable.
        }
    });
// During application shutdown:
subscription.close();
```

The no-cursor overload starts a fresh subscription. A saved cursor is opaque;
expired WAL history can trigger another snapshot. Records are whole-record
replacements, not patches; `exists() == false` is a deletion. Lifecycle uses typed
`Listing` / `Activity` enums (null when absent/not applicable), with visibility
and primitive timestamps. Optional timestamp sentinels are documented on the API.

`record.getBasicMasterdata()` returns `Optional<BasicMasterdataDecoder>`, empty
when the field is absent or the record is deleted. Named getters also work in
`Feeds.CatalogSink.write(record)`. Accessing an unselected field throws
`IllegalArgumentException`, even on a deleted record.

For fields with multiple subfields, use the generated
`record.forEachClassification((subfieldBytes, value) -> { ... })`; both arguments
are borrowed. Single-value getters reject repeated/subfield values with
`IllegalStateException` instead of dropping entries. Generic consumers can use
`record.value(Fields.X)` (typed decoder or null) and `record.forEachValue(Fields.X, consumer)`.
Omit field selection with `List.of()` to receive all fields; `Fields.ALL` is the
generated codec inventory. These APIs use the exact environment's owner schemas.
The [codec index](codecs.md) lists every named getter. Catalog message names shared
by multiple namespaces receive a namespace prefix, for example
`getOpenmdtaListing()` and `getVendorListing()`. A message named `Class` uses
`getClass_()` to avoid Java's inherited `getClass()` method.
Catalog and Stream subscriptions have no ordering relationship to each other.

## Cancellation and callback lifetime

Every read/subscription returns an `AutoCloseable` `Request`. `close()` is
idempotent, stops local delivery and sends cancellation to the gateway. It does
not wait for a server acknowledgement or interrupt a callback already running.
For scoped work, use try-with-resources as above. For an application-lifetime
subscription, retain the handle and close it during shutdown; leaving a
try-with-resources block immediately would cancel it immediately.

`completion()` completes normally on DONE and exceptionally on transport, server
or callback failure. Closing an active request cancels its completion future;
calling `completion().cancel(false)` also cancels the underlying request.
`get(timeout, unit)` timing out does not itself cancel: use a surrounding
try-with-resources block. Attach `whenComplete` when monitoring a long-lived
subscription without blocking a thread. Normal cancellation produces a
`CancellationException`, distinct from a transport/server failure.

This SDK uses synchronous callbacks with explicit borrowed lifetimes. That keeps
the decode path free of per-update objects. Java's `Flow.Publisher` is useful
when an application needs reactive composition and demand control, but downstream
buffering/asynchronous operators require owned values: copy selected primitives
into a bounded application queue first. Do not publish these mutable borrowed
views as retainable events.

## Connection state and reconnection

`Client.connect` completes after the first authentication and fails if that first
attempt fails. From then on the client reconnects by itself whenever the session
is lost, including after a missed heartbeat: it waits with a doubling ceiling
from 250 ms up to 30 s, with jitter in the upper half of each ceiling so a retry
never waits less than the one before, asks the token supplier for a fresh token,
authenticates, and replays every open subscription and pending read. A replayed
request starts over from a fresh server snapshot; the listener's `onReplay()`
default method runs first so state derived from the previous session can be
discarded, for example a `StreamListener` sees a new fence afterwards. Recovery,
snapshot and Catalog feed requests belong to one session: they fail with
"feed request connection lost" and a sink workflow restarts from its durable
state, which is why `Feeds.Stream` fails on replay instead of guessing coverage.
Requests issued while reconnecting are sent once the next session is
authenticated. The client gives up only on `close()` or when re-authentication is
refused, for example after a revoked token.

```java
client.status();                      // ConnectionStatus(state, attempt, retryAt, error)
try (var watching = client.onStatus(status -> indicator.show(status.state()))) { ... }
```

`ConnectionState` is `CONNECTING`, `READY`, `RECONNECTING` or `CLOSED`. Status
listeners run on the client's own timer thread, never while the client's lock is
held, so they may call back into the client but should not block.

## Typed source-wide Stream access

A lambda receives one complete `MarketDataUpdate`. Implement `StreamListener`
when you also need source coverage controls:

```java
var subscription = dataset.streamSubscribe(List.of(Blocks.BID_ASK), new StreamListener() {
    @Override public void onUpdate(MarketDataUpdate message) {
        message.bidAsk().ifPresent(quote -> quote.bid().ifPresent(bid -> {
            BigDecimal price = bid.price();
            // Commit this message's selected fields together with message.messageId().
        }));
    }
    @Override public void onFence(long throughMessageId) { /* initial live boundary */ }
    @Override public void onGap(long afterMessageId, long throughMessageId) { /* repair (after, through] */ }
    @Override public void onWatermark(long afterMessageId, long throughMessageId) { /* confirmed coverage */ }
});
// Keep the handle until shutdown, then close it.
subscription.close();
```

`streamRecover(start, end, blocks, listener)` delivers messages in `(start, end]`.
`streamSnapshot(blocks, listener)` calls `onSnapshotBegin(throughMessageId)`,
`onSnapshotGap(afterMessageId, throughMessageId)` for any coverage gaps, then
`onUpdate` for snapshot messages and `onSnapshotComplete()` before completing.
Stream coverage uses source message IDs; `MarketDataListener.onGap` for history
uses event times. Use `streamFeed` when you want the SDK to coordinate snapshot,
live delivery, recovery and durable checkpoints.

Protocol envelopes, template dispatch and transport callbacks are internal to
the SDK. Client code never constructs a batch decoder or checks a template ID.

Use the real alias and an available block from the linked environment/codec
index. Different environments expose different blocks. When using the borrowed
`getX()` decoder API, use generated primitive null-value constants to distinguish an absent optional price; a composite
flyweight itself is never Java `null`. See the original owner XML for composite
presence conventions. Decoder decimal values stay as mantissa/exponent, and `uint64`
IDs stay as Java `long` bit patterns: use `Long.compareUnsigned` and
`Long.toUnsignedString`, never floating-point conversion. `BigDecimal`, strings
and retained records are application choices.

`MarketDataUpdate`, `CatalogRecord`, generated decoders and every buffer
obtained from them are **borrowed until the callback returns**. They are mutable
and reused, not thread-safe. An `Optional` from a borrowed `getX()` getter contains that same
borrowed decoder: retaining the Optional does **not** preserve its value. Getters
rewind decoders for another traversal, so consume a value before calling its getter
again. Do not retain borrowed values, hand them to an executor, or
block waiting for another request on the same connection. For asynchronous work,
copy the values you need into your own bounded queue, or call `message.copy()` /
`record.copy()` for an independent typed message/record, including metadata and
all its selected fields. These copies deliberately allocate. Their SBE decoders
remain mutable: use each owned copy from one thread at a time.
Read groups/variable data in schema order; wrap/rewind before a second traversal.
Use `wrapPayload(buffer)` / other `wrapX` accessors to borrow variable data;
`getX(byte[])`, string accessors and `toString()` copy or allocate.

Unfragmented WebSocket buffers, session bodies and owner payloads are viewed
without copying. Fragmented messages are copied into a bounded buffer reused
across messages; it grows only when a larger message arrives. Decoding and batch
iteration and borrowed `getX()` getters allocate no objects after initialization.
Owned value accessors such as `bidAsk()` allocate records, Optionals and decimals. Application
code such as Optional `map` chains, capturing lambdas and string accessors may
allocate. This is **not a claim that
network delivery allocates nothing**: JDK HTTP/WebSocket/TLS, request setup,
credit sends (per consumed transport batch with v2, per 32 responses with v1), JSON controls, errors and explicit
copies can allocate. There is no per-message SDK queue or automatic record conversion.
Callbacks run serially and apply connection-wide backpressure. Use separate
connections when a slow durable sink should not delay unrelated subscriptions.

## Commands and sinks

- `latest(selector, blocks, listener)`, `latestStream(...)` and
  `timeseries(selector, from, through, blocks, listener)` deliver typed borrowed
  `MarketDataUpdate` views.
- `streamSubscribe(blocks, listener)` delivers typed messages and named
  fence/gap/watermark callbacks. `streamRecover(start, end, blocks, listener)`
  covers `(start, end]`. `streamSnapshot(blocks, listener)` delivers typed
  snapshot boundaries and messages.
- `read(selector, fields, listener)` / `read(identifiers, fields, listener)` read
  typed `CatalogRecord` views. `catalogSubscribe(fields, cursor, listener)` delivers
  them with named snapshot and cursor callbacks. The cursor may be null; stale WAL history may cause
  a new snapshot. There is no `catalogRecover` or `catalogSnapshot` command.
- `dataset.quality("DL")` / `quality("EOD")` selects configured Stream/history
  request quality.
- `metadata(listener)` delivers an owned, typed `StreamMetadata` with optional
  activity, weekly windows, exceptions, and holidays.
  `client.get(pathAndQuery)` is an allocating HTTP/JSON convenience API scoped
  to this environment. Other operations in the environment's manual contract
  do not yet have Java wrappers; there is no public raw request escape hatch.

`Request.completion()` completes on DONE and fails on server/transport/callback
errors. Closing a request cancels it. Closing a client fails its active requests.
Each session authenticates once; the token supplier is consulted for every
automatic reconnect.
`MdToken.issue` signs delegated tokens on a trusted server using the exact SBE
schemas. Never ship the DataClient private key in a client application.

Reserve **Feed** for storage workflows:

```java
var stream = dataset.streamFeed(blocks, streamSink);
var catalog = dataset.catalogFeed(fields, catalogSink);
```

`Feeds.StreamSink` writes source messages atomically, rejects older/duplicate
values **per record and block**, and persists coverage, open gaps and bootstrap
state atomically. The runner subscribes before snapshotting, repairs the
snapshot/fence interval and gaps, and checkpoints only after successful writes.
An incomplete bootstrap is repeated on restart. Callbacks borrow buffers only
until the sink method returns. Data must be durable before returning; checkpoints
follow preceding data. Replaying data after a crash must be idempotent.

`Feeds.CatalogSink` stages snapshot records and atomically swaps them with the
snapshot-complete cursor. Updates replace or delete the entire record keyed by
`recordIdentifier`; persist a cursor only after its preceding data. On restart
abandon partial staging and resume from the last committed opaque cursor.
Catalog and Stream use separate commands and sinks. They have **no shared
ordering guarantee**.

A failed feed stops and exposes the failure through `completion()`, including
when its session was lost; the client reconnects by itself, so start the feed
again using the same durable sink once `client.status()` is `READY`. There is no hidden
retry of an ambiguous mutation or an unbounded transport queue. The optional
`streamFeedMemory` and `catalogFeedMemory` helpers explicitly copy retained data,
allocate, and have no capacity limit or restart durability. Inspect their state
only while delivery is stopped, or arrange external synchronization.

For Stream memory, `records.get(key).get(Blocks.BID_ASK)` is the owned message
that last updated that block. Its other fields belong to that same original
message; they are not a synthesized current record. Catalog memory stores owned
`CatalogRecord` values with the same typed getters as callback records.

## Schemas and reproducibility

[Original XML](manual/schemas/README.md) is bundled unchanged, including every
owner schema needed to encode/decode this environment. `src/main/sbe/` contains
Java generator inputs: package names are isolated, and previously unbounded
uint32 `length`/`numInGroup` declarations receive `maxValue=2147483647`, as required
by the official generator. These changes do not change field types, offsets,
versions, sentinels or any bytes on the wire. Runtime frame/body limits remain
much smaller. Java keyword member names receive `_` in the generation XML (for example
`package_`); this also avoids an upstream SBE 1.40.2 skip-method naming bug.
Catalog model `varDataEncoding` carries UTF-8 text. Java generation annotates
that encoding so official SBE emits optional allocating String accessors as well
as zero-copy `wrapX` accessors. Runtime binary payload encodings are unchanged.

SBE's generated owner encoders are available alongside decoders. Stream batch
payloads omit the SBE header; Catalog owner payloads start with the native
four-byte blockLength/version prefix. The internal Catalog adapter handles that prefix
without copying. Encoding or decoding a field does not grant write permission;
use only operations advertised by this environment.


Feed requests negotiate SBE session v2 count/byte windows when supported, with
v1 fallback. Each feed reserves 8 MiB within a 64 MiB connection allowance and
permits 16 outstanding responses. Batched responses are delivered in wire order
as complete borrowed messages in the same order. Credits are returned only after all
callbacks in the batch finish; they are unrelated to rate limits or durable
resume pointers.
