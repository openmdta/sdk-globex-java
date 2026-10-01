package com.openmdta.sdk.p_globex;

import java.util.concurrent.CompletableFuture;

/** One request. Callbacks are serialized on the receiving connection; no per-message queue. */
public final class Request implements AutoCloseable {
    @FunctionalInterface interface Listener {
        void onResponse(Response response) throws Exception;
        default void onRegistered(Request request) {}
        default void onComplete() throws Exception {}
    }
    final Client client;
    final long id;
    final Listener listener;
    final Format[] responses;
    final CompletableFuture<Void> completion = new CompletableFuture<>();
    final AnnouncedFields fields = new AnnouncedFields();
    int consumed;
    boolean windowed;
    volatile boolean closed;
    Request(Client client, long id, Listener listener, Format[] responses) {
        this.client = client; this.id = id; this.listener = listener; this.responses = responses;
        completion.whenComplete((ignored, error) -> { if (completion.isCancelled()) close(); });
    }
    /** Completion/error of this request. Cancelling this future also cancels the wire request. */
    public CompletableFuture<Void> completion() { return completion; }
    /** Idempotently stop local delivery and send cancellation; does not wait for a server acknowledgement. */
    @Override public void close() {
        if (!closed) { closed = true; client.cancel(this); }
    }
}
