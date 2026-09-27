# Binary encoding

All integers are little-endian. Signed integers use two's complement. Use exact
64-bit integers: JavaScript numbers cannot represent every message ID or
timestamp. Decimal prices are `(mantissa: int64, exponent: int8)` and mean
`mantissa × 10^exponent`; preserve both components instead of rounding to float.

The complete [XML inventory](schemas/README.md) defines primitive widths,
composites, enums, optional sentinels, offsets, versions and variable sections.
Each XML file is self-contained. The accompanying `.layout.json` includes
resolved SBE tokens and lexical declarations; XML is authoritative for signed
and floating-point null values.

## Headers and bodies

A full SBE message begins with four `uint16` values: `blockLength`, `templateId`,
`schemaId`, `version`, in that order (8 bytes). Field offsets are relative to the
body immediately after this header. `blockLength` is the fixed portion only;
it excludes repeating groups and variable data.

Session `OpenRequest` and `Response` contain an embedded **format descriptor**
in a different order: `schemaId`, `templateId`, `version`, `blockLength`.
Their `body` variable data contains the application body **without** another SBE
header. Do not prepend the application's 8-byte header inside that body.

To decode, check schema/template/version and minimum fixed length before reading.
Read known fixed fields at their declared offsets; skip to the received acting
block length before reading variable sections. A field introduced after the
acting version is absent. Do not read it from the following variable data.

## Variable data and repeating groups

`varDataEncoding` is a `uint32` byte length followed by exactly that many bytes;
strings are UTF-8, without a terminator. Empty string/data is four zero bytes.
Group dimensions depend on the XML type: gateway `largeGroupSizeEncoding` is
`uint16 blockLength + uint32 count`; MDToken and owner schemas may use a
`uint16` count. Never substitute one dimension type for another.

For each group entry, decode its fixed block, then its nested groups/data in XML
order, before moving to the next entry. After all entries, continue with the
message's next group/data. Empty groups still carry their dimension header.
Check lengths and counts against remaining bytes before allocating. Reject
truncation, overflow, invalid UTF-8, illegal enum values and trailing bytes
where the operation's contract requires an exact body.

Optional integer fields use the XML null sentinel (by default signed minimum or
unsigned maximum), not zero. Optional floats can use NaN. Composite optionality
follows the declared null encoding of its members. A `clear` market-data field
means deletion of the value, distinct from an absent unselected field.

For the bundled canonical optional QuoteLevel, absence is encoded by the first
Decimal's null image: int64 minimum mantissa and int8 minimum exponent; remaining
bytes are zero. A zero mantissa with a regular exponent is a present zero price.

## MarketDataMessageBatch

The delivery unit is a **message**, containing metadata and its selected fields.
Iterate `batch.messages`, then each message's fields, and apply those fields
together. Never publish independent field events or combine fields from different
message IDs. A transport batch is only a container for several messages.

The indexed message/field tables below are a physical encoding of that hierarchy.
The producer emits one instrument update with its message ID and blocks; the
gateway preserves that grouping through projection and batching. Dataset, record
key and phase are shared by the messages in this batch; message ID belongs to the
message, and event time belongs to each field. Selection and permissions can
remove fields. A latest snapshot contains the surviving latest fields grouped by
their original message IDs, rather than reconstructing every historical field.

This response carries three groups: message descriptors, field descriptors and
event-time gaps, then record key, dataset and payload bytes. Each message's
`firstField` and `fieldCount` select a contiguous slice of the field group.
Each field's offset/length selects bytes in the final payload blob. Bounds-check
both slices. Dispatch owner payloads by `(schemaId, templateId)` and use their
acting version/block length. Preserve the enclosing message ID and each field's
event timestamp.

The transport's `eventTimeMicros` is separate from the owner payload. The
TypeScript convenience codec may prepend this timestamp internally; that extra
8-byte prefix is **not** present in a canonical owner payload on the wire.
The owner schema in this bundle, and the field descriptor, define its layout.

## Catalog payloads

A `CatalogRecord` contains lifecycle metadata and a group of selected fields.
Each field provides its label, subfield, wire ID, fixed length and
payload. Owner Catalog payloads start with `uint16 blockLength + uint16 version`
(4 bytes), followed by owner fixed fields and variable sections. They do not
start with an 8-byte SBE message header. Resolve semantic fields using the
dataset's owner schema; numeric universe IDs and SBE template IDs are different
namespaces. Preserve record identity, lifecycle and explicit absence.

The field group's fixed entry is six bytes: `uint16 wireId`, then
`uint32 fixedLength` (maximum uint32 means variable length). Version comes from
the payload prefix. `exists`, `lifecyclePresent` and `visible` are one-byte
booleans, 0=false and 1=true. When lifecycle is present, listing is 0=not listed
or 1=listed; activity is 0=unknown, 1=active, 2=inactive. Not-listed requires
activity=0. If lifecyclePresent=0, ignore its lifecycle values. Phase is
1=snapshot or 2=update. Hide/hidden times use the uint64 maximum null sentinel.

## Time and identity

Event times and historical bounds use Unix **microseconds**. Token lifetime uses
Unix **seconds**; service deadlines and keyfigure price cutoffs use **milliseconds**.
Message IDs order source messages and are not timestamps. Dataset record keys
are opaque strings, scoped by the exact dataset. Persist dataset and quality
with all cursors; never reuse a cursor for another selection or environment.
