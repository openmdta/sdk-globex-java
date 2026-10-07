package com.openmdta.sdk.p_globex;
public final class Environment {
    private Environment() {}
    public static final String NAME = "globex";
    public static final String REVISION = "4f6652db8a93f8f2a4ad6218941fd2697ae34856c8c691fe477c4026346b882b";
    public static final String AUDIENCE = "openmdta-data:globex";
    public static final java.net.URI WEBSOCKET = java.net.URI.create("wss://globex.openmdta.com/api/v1/ws");
    public static final java.net.URI HTTP = java.net.URI.create("https://globex.openmdta.com/explorer");
}
