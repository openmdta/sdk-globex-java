# globex — Java 17 client

This package targets applied revision `fd61b179330fb830d802b79c04082fdd15dfb6e65f685fb597a8df673bec9191`. Its endpoints, datasets,
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
import java.util.List;
import java.util.concurrent.TimeUnit;

try (var client = Client.connect(tokenBytes).join()) {
    var dataset = client.dataset("YOUR_DATASET_ALIAS");
    var apple = MarketSelector.isin("US0378331005").venue("XNAS");
    try (var request = dataset.latest(apple, List.of(Blocks.BID_ASK), update -> {
        if (update.cleared(Blocks.BID_ASK)) {
            // Delete the stored quote for update.recordKeyBytes().
            return;
        }
        var quote = update.value(Blocks.BID_ASK); // inferred BidAskDecoder
        if (quote != null) {
            long price = quote.bid().price().mantissa();
            byte exponent = quote.bid().price().exponent();
            // Consume/store primitives before this callback returns.
        }
    })) {
        request.completion().get(10, TimeUnit.SECONDS);
    }
}
```

`latestStream` uses the same typed callback for the initial state and subsequent
updates. `timeseries(selector, from, through, blocks, listener)` uses `Instant`
bounds and the same typed values; a `MarketDataListener` can also implement
`onGap(fromEventTimeMicros, throughEventTimeMicros)` for history coverage gaps.

Each `MarketDataUpdate` is **one source message**, with a record key, dataset,
message ID, snapshot flag and per-block event times. `value(Blocks.X)` returns
the generated decoder type for X, or null if that block is absent/cleared.
`changed(X)` and `cleared(X)` distinguish no change from deletion. Multiple blocks
can be selected together; accessing an unselected descriptor fails locally.
Decoders and buffers are allocated at request setup and reused on delivery.
There is no template-ID dispatch, decoder construction or cast in application code.

## Typed Catalog reads and subscriptions

```java
dataset.read(apple, List.of(Fields.OPENMDTA__BASIC_MASTERDATA), record -> {
    var master = record.value(Fields.OPENMDTA__BASIC_MASTERDATA);
    if (record.exists() && master != null) {
        String name = master.name(); // typed string accessor; deliberately allocates
    }
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
            var master = record.value(Fields.OPENMDTA__BASIC_MASTERDATA);
            // Replace this record in the target store, including absent fields.
            // Copy only the values you retain; record and master are borrowed.
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

`record.value(Fields.X)` returns X's generated decoder or null when absent.
For fields with multiple subfields, use
`record.forEachValue(Fields.X, (subfieldBytes, value) -> { ... })`; both arguments
are borrowed. `value` rejects multi-valued fields instead of dropping entries.
Omit field selection with `List.of()` to receive all fields; `Fields.ALL` is the
generated codec inventory. These APIs use the exact environment's owner schemas.
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
views as retainable events. There is no automatic reconnect or retry.

## Low-level Stream access

For source-wide subscriptions, recovery, and manual protocol handling, the
low-level Stream commands still expose `Response` and `BatchView`:

```java
import com.openmdta.sdk.p_globex.*;
import java.util.List;

var quote = Blocks.BID_ASK.decoder().get(); // allocate once
var batch = new BatchView();              // reuse for every delivery
BatchView.FieldConsumer consume = (messageId, field) -> {
    if (field.clear()) {
        // Remove this block's previous value in your store.
        return;
    }
    field.decode(Blocks.BID_ASK, quote);
    long mantissa = quote.bid().price().mantissa();
    byte exponent = quote.bid().price().exponent();
    // Store primitive values, or finish using this view before returning.
};

try (var client = Client.connect(tokenBytes).join()) {
    try (var request = client.dataset("YOUR_DATASET_ALIAS")
            .streamSubscribe(List.of(Blocks.BID_ASK), response -> {
                if (response.templateId() == 108) {
                    batch.wrap(response).forEachField(consume);
                } else {
                    // Decode FeedControl using a reusable generated decoder.
                    // Controls carry the subscription fence, gaps and coverage.
                }
            })) {
        request.completion().join();
    }
}
```

Use the real alias and an available block from the linked environment/codec
index. Different environments expose different blocks. Use generated primitive
null-value constants to distinguish an absent optional price; a composite
flyweight itself is never Java `null`. See the original owner XML for composite
presence conventions. Decimal values stay as mantissa/exponent, and `uint64`
IDs stay as Java `long` bit patterns: use `Long.compareUnsigned` and
`Long.toUnsignedString`, never floating-point conversion. `BigDecimal`, strings
and retained records are application choices.

`MarketDataUpdate`, `CatalogRecord`, `Response`, `BatchView`, `BatchView.Field`, generated decoders and every buffer
obtained from them are **borrowed until the callback returns**. They are mutable
and reused, not thread-safe. Do not retain them, hand them to an executor, or
block waiting for another request on the same connection. For asynchronous work,
copy the fields you need into your own bounded queue, or call `response.copy()`
for an owned binary body. The latter deliberately allocates. For direct SBE use,
`response.decode(yourReusableDecoder)` wraps the headerless application body.
Read groups/variable data in schema order; wrap/rewind before a second traversal.
Use `wrapPayload(buffer)` / other `wrapX` accessors to borrow variable data;
`getX(byte[])`, string accessors and `toString()` copy or allocate.

Unfragmented WebSocket buffers, session bodies and owner payloads are viewed
without copying. Fragmented messages are copied into a bounded buffer reused
across messages; it grows only when a larger message arrives. Decoding and batch
iteration allocate no objects after initialization. This is **not a claim that
network delivery allocates nothing**: JDK HTTP/WebSocket/TLS, request setup,
credit sends (per consumed transport batch with v2, per 32 responses with v1), JSON controls, errors and explicit
copies can allocate. There is no per-message SDK queue or map/record conversion.
Callbacks run serially and apply connection-wide backpressure. Use separate
connections when a slow durable sink should not delay unrelated subscriptions.

## Commands and sinks

- `latest(selector, blocks, listener)`, `latestStream(...)` and
  `timeseries(selector, from, through, blocks, listener)` deliver typed borrowed
  `MarketDataUpdate` views.
- `streamSubscribe(blocks, listener)` delivers batches and fence/gap/watermark
  controls. `streamRecover(start, end, blocks, listener)` covers `(start, end]`.
  `streamSnapshot(blocks, listener)` starts with a snapshot header, then batches.
- `read(selector, fields, listener)` / `read(identifiers, fields, listener)` read
  typed `CatalogRecord` views. `catalogSubscribe(fields, cursor, listener)` delivers
  them with named snapshot and cursor callbacks. The cursor may be null; stale WAL history may cause
  a new snapshot. There is no `catalogRecover` or `catalogSnapshot` command.
- `dataset.quality("DL")` / `quality("EOD")` selects configured Stream/history
  request quality. `metadata(listener)` queries that source.
- `client.request(operationName, generatedEncoder, listener)` covers **every**
  advertised operation, including candles, pagination, listing, search,
  keyfigures and service calls. Build exactly the JSON body/fingerprint from
  [the operation contract](manual/operations/index.md); runtime capability and
  authorization checks still apply. The encoder body is copied before return,
  so its buffer can then be reused. `client.get(pathAndQuery)` is an allocating
  HTTP/JSON convenience API scoped to this environment.

`Request.completion()` completes on DONE and fails on server/transport/callback
errors. Closing a request cancels it. Closing a client fails its active requests.
Authentication happens once per connection; refresh tokens by reconnecting.
`MdToken.issue` signs delegated tokens on a trusted server using the exact SBE
schemas. Never ship the main signing secret in a client application.

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

A failed feed stops and exposes the failure through `completion()`. Reconnect
with backoff and start it again using the same durable sink. There is no hidden
retry of an ambiguous mutation or an unbounded transport queue. The optional
`streamFeedMemory` and `catalogFeedMemory` helpers explicitly copy retained data,
allocate, and have no capacity limit or restart durability. Inspect their state
only while delivery is stopped, or arrange external synchronization.

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
four-byte blockLength/version prefix. `CatalogField.wrap` handles that prefix
without copying. Encoding or decoding a field does not grant write permission;
use only operations advertised by this environment.


Feed requests negotiate SBE session v2 count/byte windows when supported, with
v1 fallback. Each feed reserves 8 MiB within a 64 MiB connection allowance and
permits 16 outstanding responses. Batched responses are delivered in wire order
through the same borrowed response views. Credits are returned only after all
callbacks in the batch finish; they are unrelated to rate limits or durable
resume pointers.
