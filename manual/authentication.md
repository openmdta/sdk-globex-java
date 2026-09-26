# Authentication and MDToken

Provision a data-client ID and its 32-byte signing secret through the environment
administrator. The supplied secret is unpadded base64url; decode it to exactly
32 bytes before HMAC. The audience must exactly match [environment.md](environment.md).
Credentials are deliberately absent from this package.

A trusted server can authenticate with UTF-8 bytes
`main:<client-id>:<base64url-secret>`. Keep that credential on the server.
For browser/delegated access mint a short-lived MDToken. Send its raw binary
bytes in session AUTH. HTTP APIs use `Authorization: Bearer <base64url(token)>`
with unpadded base64url. Do not send JSON or the base64 text inside binary AUTH.

## Build a delegated token

[mdtoken.xml](schemas/mdtoken.xml) contains both the envelope and claims schema.
All lengths count bytes, not characters. This is a binary HMAC token, not JWT.

1. Encode claims header `(blockLength=16, templateId=2, schemaId=8, version)`.
   Use version 0 for grants/audience, 1 when allowed origins are present, or 2
   when a delegated rate limit is present. Encode signed `int64 issuedAt` and
   `expiresAt` in Unix seconds. Require `issuedAt <= now < expiresAt` and a
   positive lifetime at most 300 seconds.
2. Encode grants group dimensions `uint16 blockLength=1`, `uint16 count`.
   Each entry is `uint8 quality` followed by `uint32 length + UTF-8 package`.
   Quality: 0=`*`, 1=`RT`, 2=`DL`, 3=`EOD`. A package is `NAMESPACE:PACKAGE` or
   `*`. Grant only permissions allowed by the data client's current ceiling.
3. For claims version >=1, encode origins: `uint16 blockLength=0`, `uint16 count`,
   then each length-prefixed UTF-8 origin. Match full browser origins (scheme,
   host and optional port); an origin restriction is not a path allowlist.
4. Encode the length-prefixed UTF-8 audience.
5. For version 2 append length-prefixed rate-limit bytes. Inside those bytes:
   length-prefixed bucket ID, `uint64 bucketSizeMicrotokens`,
   `uint64 refillPerSecondMicrotokens`. One token is 1,000,000 microtokens.
   Omitting rate limits uses version 0/1, not an invented empty version-2 value.
6. Compress the **entire claims message including its header** as zlib (RFC1950
   envelope, DEFLATE payload). The server uses compression level 6; other valid
   zlib encodings are accepted because the MAC covers the actual compressed bytes.
7. Build envelope header `(0,1,8,0)`; the envelope stays at version 0. Append
   length-prefixed UTF-8 client ID, length-prefixed compressed claims, and
   `uint32 32` for the MAC length.
8. Compute HMAC-SHA256 using the decoded secret over the ASCII bytes
   `OpenMDTA-MDToken-v1` followed by one zero byte, then **all envelope bytes so
   far, including the MAC length**. Append the 32-byte MAC. Do not include the
   MAC bytes themselves in the signed input.

Limits: 128 bytes client ID, 256 bytes audience, 256 grants, 65,536 uncompressed
claim bytes, 6,144 binary token bytes and 8,192 bearer characters. Decompression
must be bounded and consume the complete compressed input. Verify HMAC with a
constant-time comparison before trusting claims. Token expiry, revocation,
disabled clients, mismatched audience/origin and permissions can end requests.
License requirements can differ by record and field: a valid session does not
imply access to every configured dataset. Rate limits can reject work; HTTP
429 may include Retry-After, while session requests report an error.
