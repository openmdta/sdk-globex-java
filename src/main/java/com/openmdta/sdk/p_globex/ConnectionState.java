package com.openmdta.sdk.p_globex;

/**
 * {@code CONNECTING} until the first authentication, {@code READY} while a session is authenticated and
 * its heartbeat is current, {@code RECONNECTING} between a lost session and the next authentication,
 * and {@code CLOSED} once the client was closed or gave up after an authentication failure.
 */
public enum ConnectionState { CONNECTING, READY, RECONNECTING, CLOSED }
