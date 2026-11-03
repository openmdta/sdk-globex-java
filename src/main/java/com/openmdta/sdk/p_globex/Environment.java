package com.openmdta.sdk.p_globex;
public final class Environment {
    private Environment() {}
    public static final String NAME = "globex";
    public static final String REVISION = "f92f98d2ea4cc7224a2cbe78c2dbf14d71fc7bbaabfeb2575cabdcfce7c538ac";
    public static final String AUDIENCE = "openmdta-data:globex";
    public static final java.net.URI WEBSOCKET = java.net.URI.create("wss://globex.openmdta.com/api/v1/ws");
    public static final java.net.URI HTTP = java.net.URI.create("https://globex.openmdta.com/explorer");
}
