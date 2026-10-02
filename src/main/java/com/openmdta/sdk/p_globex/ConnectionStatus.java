package com.openmdta.sdk.p_globex;

import java.time.Instant;

/**
 * One immutable lifecycle observation.
 *
 * @param attempt consecutive failed connection attempts since the last authenticated session; 0 while ready
 * @param retryAt when the next attempt starts while reconnecting, otherwise null
 * @param error what ended the last session or closed the client for good; null while ready or after close()
 */
public record ConnectionStatus(ConnectionState state, int attempt, Instant retryAt, Throwable error) {}
