# Byte examples

Hex pairs are bytes in transmission order. These examples contain no real
credential. Use the environment's dataset/quality when building real requests.

## Cancel request

Cancel target request 2 using cancellation request ID 3:

```text
10 00 03 00 05 00 00 00
03 00 00 00 00 00 00 00
02 00 00 00 00 00 00 00
```

Header: fixed length 16, template 3, schema 5, version 0. No variable data.

## Response credits

Grant one response credit to request 2:

```text
0c 00 04 00 05 00 00 00
02 00 00 00 00 00 00 00
01 00 00 00
```

## Empty successful terminal response

Request 2 completed, with no application body or error:

```text
11 00 65 00 05 00 00 00
02 00 00 00 00 00 00 00 02
00 00 00 00 00 00 00 00
00 00 00 00 00 00 00 00
```

After its 17-byte fixed block come two uint32 zero lengths: body and error.

## Feed-live application body

For SBE template ID 0 (mask 1), dataset `TEST@feed`, quality `RT`, the headerless body is:

```text
01 00 00 00 00 00 00 00
09 00 00 00 54 45 53 54 40 66 65 65 64
02 00 00 00 52 54
```

Its format descriptor is `66 00 0f 00 12 00 08 00`
(schema 102, template 15, version 18, block length 8). Put that descriptor at
OpenRequest body offset 32. Append a uint32 length of 27 before the application
body. The full OpenRequest starts with `28 00 02 00 05 00 00 00`, then request
ID and 24 trace bytes. Do not copy a gateway SBE header into the nested body.

Negative checks: reject a 7-byte session header, a length exceeding remaining
bytes, zero response credit, duplicate active request ID, unexpected initial
feed message instead of a fence, and recovery messages outside `(after,through]`.
