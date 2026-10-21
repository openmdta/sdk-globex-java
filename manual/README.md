# globex — manual client implementation

This package describes gateway **gateway**, applied revision `3edddec878a2cc5612f4c13c46ccebec037e38eabc0d913538cce3ae1c272d5b`. Implement a client using standard WebSocket, byte-buffer, JSON, zlib and Ed25519 libraries. No generated SDK is required.

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
