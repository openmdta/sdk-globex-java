# globex — manual client implementation

This package describes gateway **gateway**, applied revision `cfd52bd0d0fd8f9c52ba809a5c446dee3ee444469445b4e5d78017657efd572f`. Implement a client using standard WebSocket, byte-buffer, JSON, zlib and HMAC libraries. No generated SDK is required.

Read in order:

1. [Environment and available datasets](environment.md)
2. [Authentication](authentication.md)
3. [Session state machine](session.md)
4. [Binary encoding](encoding.md)
5. [Operations](operations/index.md)
6. [Persistence and recovery](persistence-and-recovery.md)
7. [Binary bodies](binary-bodies.md) and [HTTP reads](http.md)
8. [Compatibility and implementation checks](compatibility-and-limitations.md)

The [complete schemas](schemas/README.md), [byte examples](examples/README.md) and [machine-readable contract](contract.json) are part of this specification.
