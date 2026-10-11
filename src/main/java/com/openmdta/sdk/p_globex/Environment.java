package com.openmdta.sdk.p_globex;
public final class Environment {
    private Environment() {}
    public static final String NAME = "globex";
    public static final String REVISION = "78896eec58eea7aadb7d3e876ffe013a9083447463c830bdb686402e4af7e21e";
    public static final String AUDIENCE = "openmdta-data:globex";
    public static final java.net.URI WEBSOCKET = java.net.URI.create("wss://globex.openmdta.com/api/v1/ws");
    public static final java.net.URI HTTP = java.net.URI.create("https://globex.openmdta.com/explorer");
}
