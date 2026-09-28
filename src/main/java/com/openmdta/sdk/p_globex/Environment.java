package com.openmdta.sdk.p_globex;
public final class Environment {
    private Environment() {}
    public static final String NAME = "globex";
    public static final String REVISION = "1a16d092a6e0de9afc294551adc7cb0e3f73406d99fb1efd3ac8c206b81fe732";
    public static final String AUDIENCE = "openmdta-data:globex";
    public static final java.net.URI WEBSOCKET = java.net.URI.create("wss://globex.openmdta.com/api/v1/ws");
    public static final java.net.URI HTTP = java.net.URI.create("https://globex.openmdta.com/explorer");
}
