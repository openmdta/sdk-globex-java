# Authentication and MDToken

Your environment administrator registers a data client with your Ed25519 public
key and gives you its client ID (`namespace/name`). Keep the private key on your
server. It is the 32-byte Ed25519 seed as unpadded base64url, which is also a
JWK's `d` member; the public key is the JWK's `x` member. The audience must
exactly match [environment.md](environment.md). Credentials are deliberately
absent from this package.

Every connection authenticates with an MDToken. A trusted server signs its own
short-lived tokens; for browser or delegated access it signs a token with only
the permissions the end user needs. Send the raw binary token bytes in session
AUTH. HTTP APIs use `Authorization: Bearer <base64url(token)>` with unpadded
base64url. Do not send JSON or the base64 text inside binary AUTH.

## Grants and service access

A grant is `*`, `NAMESPACE:LICENSE` or `NAMESPACE:LICENSE:RT|DL|EOD`. There are
no partial wildcards. A grant without a quality is an untimed package: it passes
Catalog and identifier checks, never a timed service. A timed grant also passes
the untimed checks of its package. A token may contain only grants its data
client holds; `*` in a token means all grants of the client.

Which services a client may call on which datasets is configured on the data
client, not in the token. Service kinds are `catalog`, `search`, `latest`,
`timeseries`, `keyfigures` and `observation`. A request to a service the client
may not use fails even when the licenses would allow the data.

## Build a token

[mdtoken.xml](schemas/mdtoken.xml) contains both the envelope and claims schema.
All lengths count bytes, not characters. This is a binary Ed25519 token, not JWT.

1. Encode claims header `(blockLength=16, templateId=2, schemaId=8, version=3)`.
   Encode signed `int64 issuedAt` and `expiresAt` in Unix seconds. Require
   `issuedAt <= now < expiresAt` and a positive lifetime at most 300 seconds.
2. Encode grants group dimensions `uint16 blockLength=1`, `uint16 count`.
   Each entry is `uint8 quality` followed by `uint32 length + UTF-8 package`.
   Quality: 0=untimed (or the package `*`), 1=`RT`, 2=`DL`, 3=`EOD`. The package
   is `NAMESPACE:LICENSE` or `*`; `*` requires quality 0.
3. Encode origins: `uint16 blockLength=0`, `uint16 count`, then each
   length-prefixed UTF-8 origin. The group is always present. Match full browser
   origins (scheme, host and optional port); an empty group permits only
   requests without an Origin header.
4. Encode the length-prefixed UTF-8 audience.
5. Append length-prefixed rate-limit bytes, empty when there is no delegated
   limit. Otherwise: length-prefixed bucket ID, `uint64 bucketSizeMicrotokens`,
   `uint64 refillPerSecondMicrotokens`. One token is 1,000,000 microtokens.
6. Compress the **entire claims message including its header** as zlib (RFC1950
   envelope, DEFLATE payload). The server uses compression level 6; other valid
   zlib encodings are accepted because the signature covers the actual bytes.
7. Build envelope header `(0,1,8,1)`. Append length-prefixed UTF-8 client ID,
   length-prefixed compressed claims, and `uint32 64` for the signature length.
8. Sign with Ed25519 (RFC 8032, pure) over the ASCII bytes `OpenMDTA-MDToken-v2`
   followed by one zero byte, then **all envelope bytes so far, including the
   signature length**. Append the 64-byte signature.

Limits: 128 bytes client ID, 256 bytes audience, 256 grants, 32 origins, 65,536
uncompressed claim bytes, 6,144 binary token bytes and 8,192 bearer characters.
Decompression must be bounded and consume the complete compressed input.
Token expiry, key rotation, disabled clients, mismatched audience/origin,
service access and permissions can end requests. License requirements can
differ by record and field: a valid session does not imply access to every
configured dataset. Rate limits can reject work; HTTP 429 may include
Retry-After, while session requests report an error.
