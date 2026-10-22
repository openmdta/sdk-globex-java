package com.openmdta.sdk.p_globex;
public final class Environment {
    private Environment() {}
    public static final String NAME = "globex";
    public static final String REVISION = "7fae95b6813086d8c3ca191e48e5e73d3213e8c1abaeb5fb9a1a5c6aa0a2e4de";
    public static final String AUDIENCE = "openmdta-data:globex";
    public static final java.net.URI WEBSOCKET = java.net.URI.create("wss://globex.openmdta.com/api/v1/ws");
    public static final java.net.URI HTTP = java.net.URI.create("https://globex.openmdta.com/explorer");
}
