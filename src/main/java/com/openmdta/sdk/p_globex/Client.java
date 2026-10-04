package com.openmdta.sdk.p_globex;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.agrona.DirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageEncoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.DatasetFieldsDecoder;
import com.openmdta.sdk.p_globex.sbe.sbe_websocket.*;

/**
 * One logical connection. Callbacks borrow transport bytes and run serially. A lost session is
 * re-established automatically with increasing delays; open subscriptions and pending reads are replayed.
 */
public final class Client implements AutoCloseable {
    private static final CompletionStage<Void> CONSUMED = CompletableFuture.completedFuture(null);
    private static final int MAX_FRAME = Contract.ROOT.path("runtime").path("limits").path("websocketMessageBytes").asInt();
    private static final int MAX_BODY = Contract.ROOT.path("runtime").path("limits").path("applicationBodyBytes").asInt();
    private static final int CREDITS = Math.min(32, Contract.ROOT.path("runtime").path("limits").path("responseCredits").asInt());
    private static final String WINDOW_PROTOCOL = "openmdta.sbe-session.v2";
    private static final int WINDOW_BYTES = 8 * 1024 * 1024;
    private int reservedWindowBytes;
    private final UnsafeBuffer nestedBody = new UnsafeBuffer(0, 0);
    private final HttpClient http;
    private final Supplier<? extends CompletionStage<byte[]>> tokens;
    private final Long2ObjectHashMap<Request> pending = new Long2ObjectHashMap<>();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "openmdta-connection"); t.setDaemon(true); return t; });
    private final CompletableFuture<Client> ready = new CompletableFuture<>();
    private final UnsafeBuffer incoming = new UnsafeBuffer(0, 0), body = new UnsafeBuffer(0, 0), errorText = new UnsafeBuffer(0, 0);
    private final MessageHeaderDecoder header = new MessageHeaderDecoder();
    private final ResponseDecoder envelope = new ResponseDecoder();
    private final Response response = new Response();
    private final CopyOnWriteArrayList<Consumer<ConnectionStatus>> statusListeners = new CopyOnWriteArrayList<>();
    private volatile ConnectionStatus status = new ConnectionStatus(ConnectionState.CONNECTING, 0, null, null);
    private ByteBuffer fragments;
    private WebSocket socket;
    private boolean authenticated;
    private int attempt, generation;
    boolean closed;
    private long sequence = 2, lastHeartbeat = System.nanoTime();
    private CompletableFuture<Void> sending = CompletableFuture.completedFuture(null);
    private int queuedSendBytes;

    private final WebSocket.Listener transport = new WebSocket.Listener() {
        @Override public void onOpen(WebSocket webSocket) {
            // Demand is requested once the attempt adopts this socket; the JDK reports onOpen before buildAsync completes.
            synchronized (Client.this) { if (closed) webSocket.abort(); }
        }
        @Override public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            synchronized (Client.this) {
                if (closed || webSocket != socket) return CONSUMED;
                try {
                    int accumulated = fragments == null ? 0 : fragments.position();
                    int length = Math.addExact(accumulated, data.remaining());
                    if (length > MAX_FRAME) throw new IOException("Oversized WebSocket message");
                    if (accumulated == 0 && last) {
                        incoming.wrap(data, data.position(), data.remaining());
                    } else {
                        if (fragments == null || fragments.capacity() < length) {
                            ByteBuffer grown = ByteBuffer.allocate(Math.min(MAX_FRAME, Math.max(length, fragments == null ? 65536 : fragments.capacity() * 2)));
                            if (fragments != null) { fragments.flip(); grown.put(fragments); }
                            fragments = grown;
                        }
                        fragments.put(data);
                        if (!last) { webSocket.request(1); return CONSUMED; }
                        incoming.wrap(fragments, 0, fragments.position());
                    }
                    dispatch(incoming);
                    if (fragments != null) fragments.clear();
                } catch (AuthenticationException error) { fail(error);
                } catch (Throwable error) { lost(webSocket, error); }
                if (!closed && webSocket == socket) webSocket.request(1);
                return CONSUMED;
            }
        }
        @Override public CompletionStage<?> onText(WebSocket webSocket, CharSequence text, boolean last) {
            lost(webSocket, new IOException("Unexpected WebSocket text message"));
            return CONSUMED;
        }
        @Override public CompletionStage<?> onClose(WebSocket webSocket, int code, String reason) {
            lost(webSocket, new IOException("Gateway disconnected (" + code + "): " + reason));
            return CONSUMED;
        }
        @Override public void onError(WebSocket webSocket, Throwable error) { lost(webSocket, error); }
    };

    private Client(HttpClient http, Supplier<? extends CompletionStage<byte[]>> tokens) {
        this.http = java.util.Objects.requireNonNull(http);
        this.tokens = java.util.Objects.requireNonNull(tokens);
    }
    public static CompletableFuture<Client> connect(byte[] token) {
        byte[] copy = token.clone();
        return connect(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), () -> CompletableFuture.completedFuture(copy));
    }
    /** Resolves after the first authentication and rejects if that first attempt fails; later sessions reconnect by themselves. */
    public static CompletableFuture<Client> connect(HttpClient http, Supplier<? extends CompletionStage<byte[]>> tokens) {
        Client client = new Client(http, tokens);
        synchronized (client) {
            client.open();
            client.timer.scheduleAtFixedRate(() -> {
                synchronized (client) {
                    long timeout = Contract.ROOT.path("runtime").path("limits").path("heartbeatTimeoutMillis").asLong(60_000);
                    if (!client.closed && client.authenticated && TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - client.lastHeartbeat) > timeout) {
                        client.lost(client.socket, new IOException("Gateway heartbeat timed out"));
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
        return client.ready;
    }
    /** The current lifecycle observation; cheap to poll. */
    public ConnectionStatus status() { return status; }
    /**
     * Receives every status change on the client's own timer thread, never while holding the client's
     * lock. Closing the returned handle stops delivery.
     */
    public AutoCloseable onStatus(Consumer<ConnectionStatus> listener) {
        statusListeners.add(java.util.Objects.requireNonNull(listener));
        return () -> statusListeners.remove(listener);
    }
    private void setStatus(ConnectionState state, int attempt, Instant retryAt, Throwable error) {
        ConnectionStatus next = new ConnectionStatus(state, attempt, retryAt, error);
        status = next;
        if (timer.isShutdown()) { for (Consumer<ConnectionStatus> listener : statusListeners) listener.accept(next); return; }
        timer.execute(() -> { for (Consumer<ConnectionStatus> listener : statusListeners) listener.accept(next); });
    }
    /** Starts one connection attempt; caller holds the lock. Failures are routed through lost(). */
    private void open() {
        int opened = ++generation;
        authenticated = false;
        try {
            tokens.get().thenCompose(bytes -> {
                if (bytes == null || bytes.length == 0 || bytes.length > 64 * 1024) throw new IllegalArgumentException("Invalid token length");
                return http.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10)).subprotocols(WINDOW_PROTOCOL).buildAsync(Environment.WEBSOCKET, transport).thenAccept(webSocket -> {
                    synchronized (Client.this) {
                        if (closed || opened != generation) { webSocket.abort(); return; }
                        if (!WINDOW_PROTOCOL.equals(webSocket.getSubprotocol())) { webSocket.abort(); throw new IllegalStateException("Gateway did not negotiate the SBE session protocol"); }
                        socket = webSocket;
                        sending = CompletableFuture.completedFuture(null);
                        queuedSendBytes = 0;
                        webSocket.request(1);
                        UnsafeBuffer buffer = new UnsafeBuffer(new byte[20 + bytes.length]);
                        AuthRequestEncoder auth = new AuthRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder());
                        auth.requestId(1).putToken(bytes, 0, bytes.length);
                        send(buffer.byteArray());
                    }
                });
            }).whenComplete((ignored, error) -> {
                if (error == null) return;
                synchronized (Client.this) { if (opened == generation) lost(socket, error instanceof CompletionException && error.getCause() != null ? error.getCause() : error); }
            });
        } catch (Throwable error) { lost(socket, error); }
        timer.schedule(() -> {
            synchronized (Client.this) {
                if (!closed && opened == generation && !authenticated) lost(socket, new IOException("Authentication timed out"));
            }
        }, 15, TimeUnit.SECONDS);
    }
    /** A session ended or an attempt failed. The first attempt fails for good; later losses schedule the next attempt. */
    private synchronized void lost(WebSocket which, Throwable error) {
        if (closed || (which != null && which != socket)) return;
        if (!ready.isDone()) { fail(error); return; }
        if (socket != null) { WebSocket stale = socket; socket = null; stale.abort(); }
        if (authenticated) attempt = 0;
        authenticated = false;
        fragments = null;
        Request[] active = pending.values().toArray(Request[]::new);
        for (Request request : active) {
            if (!request.replayable) {
                pending.remove(request.id);
                if (request.windowed) reservedWindowBytes -= WINDOW_BYTES;
                request.closed = true;
                request.completion.completeExceptionally(new IOException("feed request connection lost", error));
            }
        }
        // Doubling ceiling with jitter in its upper half, so every retry waits at least as long as the previous one.
        long ceiling = Math.min(30_000L, 250L << Math.min(attempt, 16));
        long delay = ceiling / 2 + ThreadLocalRandom.current().nextLong(ceiling / 2 + 1);
        attempt++;
        setStatus(ConnectionState.RECONNECTING, attempt, Instant.now().plusMillis(delay), error);
        int scheduled = generation;
        timer.schedule(() -> {
            synchronized (Client.this) { if (!closed && scheduled == generation) open(); }
        }, delay, TimeUnit.MILLISECONDS);
    }
    /** The gateway accepted the token: replay what the previous session carried. */
    private void established() {
        authenticated = true;
        attempt = 0;
        lastHeartbeat = System.nanoTime();
        boolean replay = ready.isDone();
        setStatus(ConnectionState.READY, 0, null, null);
        if (!replay) { ready.complete(this); return; }
        for (Request request : pending.values().toArray(Request[]::new)) {
            try {
                request.fields.clear();
                request.consumed = 0;
                request.listener.onReplay();
            } catch (Throwable failure) {
                pending.remove(request.id);
                if (request.windowed) reservedWindowBytes -= WINDOW_BYTES;
                request.closed = true;
                request.completion.completeExceptionally(failure);
                continue;
            }
            send(request.open);
            if (request.windowed) send(window(request.id)); else credit(request.id, CREDITS);
        }
    }
    public Dataset datasetGlobex() {
        return dataset("globex");
    }
    public Dataset datasetLus() {
        return dataset("lus").quality("DL");
    }
    public Dataset datasetXetra() {
        return dataset("xetra");
    }

    public Dataset dataset(String alias) {
        JsonNode binding = Contract.ROOT.path("environment").path("datasets").get(alias);
        if (binding == null) throw new IllegalArgumentException("Dataset not exposed by this environment: " + alias);
        return new Dataset(this, binding);
    }

    /** Encode with a generated SBE encoder; this copies its completed body before returning. */
    synchronized Request request(String operation, MessageEncoderFlyweight encoder, Request.Listener listener) {
        if (closed || !ready.isDone() || ready.isCompletedExceptionally()) throw new IllegalStateException("Client is not connected");
        if (pending.size() >= Contract.ROOT.path("runtime").path("limits").path("activeRequests").asInt(64)) throw new IllegalStateException("Too many active requests");
        Format format = Contract.OPERATIONS.get(operation);
        if (format == null || format.schemaId() != encoder.sbeSchemaId() || format.templateId() != encoder.sbeTemplateId() || format.blockLength() != encoder.sbeBlockLength()) throw new IllegalArgumentException("Encoder does not match advertised operation");
        if (encoder.encodedLength() > MAX_BODY || encoder.encodedLength() < format.blockLength()) throw new IllegalArgumentException("Invalid application body length");
        if (sequence < 2) throw new IllegalStateException("Request IDs exhausted; reconnect");
        long id = sequence++;
        Request request = new Request(this, id, java.util.Objects.requireNonNull(listener), Contract.RESPONSES.get(operation));
        UnsafeBuffer buffer = new UnsafeBuffer(new byte[52 + encoder.encodedLength()]);
        OpenRequestEncoder open = new OpenRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder()).requestId(id);
        open.format().schemaId(format.schemaId()).templateId(format.templateId()).version(format.version()).blockLength(format.blockLength());
        open.putBody(encoder.buffer(), encoder.offset(), encoder.encodedLength());
        request.open = buffer.byteArray();
        request.windowed = operation.startsWith("feed-") || operation.equals("catalog-feed");
        // Recovery, snapshot and Catalog feed requests belong to one session; sink workflows restart them from durable state.
        request.replayable = !(operation.equals("feed-recovery") || operation.equals("feed-snapshot") || operation.equals("catalog-feed"));
        if (request.windowed) {
            if ((long) reservedWindowBytes + WINDOW_BYTES > 64 * 1024 * 1024) throw new IllegalStateException("Connection feed buffer capacity reached");
            reservedWindowBytes += WINDOW_BYTES;
        }
        listener.onRegistered(request);
        pending.put(id, request);
        if (!authenticated) {
            if (!request.replayable) { pending.remove(id); reservedWindowBytes -= WINDOW_BYTES; throw new IllegalStateException("Client is reconnecting"); }
            return request; // sent once the next session is authenticated
        }
        send(request.open);
        if (request.windowed) send(window(id)); else credit(id, CREDITS);
        return request;
    }
    private static byte[] window(long id) {
        UnsafeBuffer window = new UnsafeBuffer(new byte[28]);
        new ResponseWindowEncoder().wrapAndApplyHeader(window, 0, new MessageHeaderEncoder())
            .targetId(id).responses(16).bytes(WINDOW_BYTES).maxBodyBytes(Math.min(MAX_BODY, MAX_FRAME - 128));
        return window.byteArray();
    }
    private synchronized void credit(long id, int count) {
        if (closed || !pending.containsKey(id)) return;
        UnsafeBuffer buffer = new UnsafeBuffer(new byte[20]);
        new CreditRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder()).targetId(id).credits(count);
        send(buffer.byteArray());
    }
    synchronized void cancel(Request request) {
        Request removed = pending.remove(request.id);
        if (removed != null && removed.windowed) reservedWindowBytes -= WINDOW_BYTES;
        if (removed != null && !closed && authenticated) {
            UnsafeBuffer buffer = new UnsafeBuffer(new byte[24]);
            new CancelRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder()).requestId(sequence++).targetId(request.id);
            send(buffer.byteArray());
        }
        request.completion.cancel(false);
    }
    /** Queues a frame on the current socket only; a frame for a lost session is dropped and replay re-sends what matters. */
    private synchronized void send(byte[] bytes) {
        WebSocket target = socket;
        if (closed || target == null) return;
        // These unchanged templates retain their v0 acting version even when
        // generated from the extended session schema, including v1 fallback.
        if (bytes.length >= 8 && bytes[2] >= 1 && bytes[2] <= 4 && bytes[3] == 0) { bytes[6] = 0; bytes[7] = 0; }
        if ((long) queuedSendBytes + bytes.length > 16 * 1024 * 1024) { lost(target, new IOException("Outgoing queue limit exceeded")); return; }
        queuedSendBytes += bytes.length;
        sending = sending.thenCompose(ignored -> target.sendBinary(ByteBuffer.wrap(bytes), true)).thenAccept(ignored -> {});
        sending.whenComplete((ignored, error) -> {
            synchronized (this) {
                if (target == socket) queuedSendBytes -= bytes.length;
                if (error != null) lost(target, error);
            }
        });
    }
    private void dispatch(DirectBuffer frame) throws Exception {
        frame.boundsCheck(0, MessageHeaderDecoder.ENCODED_LENGTH);
        header.wrap(frame, 0);
        if (header.schemaId() != 5 || header.version() != 0) throw new IOException("Invalid session schema");
        if (header.templateId() == CancelResponseDecoder.TEMPLATE_ID) {
            if (header.blockLength() != CancelResponseDecoder.BLOCK_LENGTH || frame.capacity() != 25) throw new IOException("Invalid cancel response");
            return;
        }
        if (header.templateId() != ResponseDecoder.TEMPLATE_ID || header.blockLength() != ResponseDecoder.BLOCK_LENGTH) throw new IOException("Invalid session response");
        envelope.wrap(frame, 8, header.blockLength(), header.version());
        long id = envelope.requestId();
        Status status = envelope.status();
        envelope.wrapBody(body); envelope.wrapError(errorText);
        if (envelope.limit() != frame.capacity() || body.capacity() > MAX_BODY) throw new IOException("Malformed response length");
        if (id == 1) {
            if (status != Status.CONTINUE || body.capacity() != 0) throw new AuthenticationException("Authentication failed: " + errorText.getStringWithoutLengthUtf8(0, errorText.capacity()));
            established(); return;
        }
        lastHeartbeat = System.nanoTime();
        Request target = pending.get(id);
        if (target == null || target.closed) return;
        try {
            if (status == Status.ERROR) throw new RequestException(errorText.getStringWithoutLengthUtf8(0, errorText.capacity()));
            if (status != Status.CONTINUE && status != Status.DONE) throw new IOException("Invalid response status");
            if (body.capacity() != 0) {
                var format = envelope.format();
                if (format.schemaId() == 5 && format.templateId() == 103) {
                    if (!target.windowed || format.version() != 1 || format.blockLength() != 0 || body.capacity() < 6) throw new IOException("Invalid response batch");
                    var order = java.nio.ByteOrder.LITTLE_ENDIAN;
                    if (body.getShort(0, order) != 8) throw new IOException("Invalid batch group length");
                    int count = body.getInt(2, order), offset = 6;
                    if (count < 1 || count > 64) throw new IOException("Invalid batch count");
                    for (int index = 0; index < count; index++) {
                        body.boundsCheck(offset, 12);
                        int schema = body.getShort(offset, order) & 0xffff, template = body.getShort(offset + 2, order) & 0xffff;
                        int version = body.getShort(offset + 4, order) & 0xffff, fixed = body.getShort(offset + 6, order) & 0xffff;
                        int size = body.getInt(offset + 8, order); offset += 12;
                        if (size < fixed || size > body.capacity() - offset || (schema == 5 && template == 103)) throw new IOException("Invalid nested response body");
                        nestedBody.wrap(body, offset, size); offset += size;
                        deliver(target, schema, template, version, fixed, nestedBody);
                        if (target.closed || closed) return;
                    }
                    if (offset != body.capacity()) throw new IOException("Batch has trailing bytes");
                } else deliver(target, format.schemaId(), format.templateId(), format.version(), format.blockLength(), body);
            }
            if (target.closed || closed) return;
            if (status == Status.DONE) {
                target.listener.onComplete();
                if (target.closed || closed) return;
                if (pending.remove(id) != null && target.windowed) reservedWindowBytes -= WINDOW_BYTES;
                target.closed = true; target.completion.complete(null);
            } else if (target.windowed && pending.containsKey(id)) {
                // All callbacks in the outer batch have finished synchronously.
                UnsafeBuffer released = new UnsafeBuffer(new byte[24]);
                new ReleaseResponsesEncoder().wrapAndApplyHeader(released, 0, new MessageHeaderEncoder())
                    .targetId(id).responses(1).bytes(body.capacity() + 128);
                send(released.byteArray());
            } else if (++target.consumed == CREDITS) {
                target.consumed = 0; credit(id, CREDITS);
            }
        } catch (Throwable failure) {
            target.completion.completeExceptionally(failure);
            target.close();
        }
    }
    private void deliver(Request target, int schema, int template, int version, int fixed, DirectBuffer bytes) throws Exception {
        boolean allowed = false;
        for (Format expected : target.responses) if (expected.schemaId() == schema && expected.templateId() == template) { allowed = true; break; }
        if (!allowed) throw new IOException("Response format does not belong to request");
        if (schema == DatasetFieldsDecoder.SCHEMA_ID && template == DatasetFieldsDecoder.TEMPLATE_ID) {
            target.fields.absorb(version, fixed, bytes);
            return;
        }
        response.wrap(schema, template, version, fixed, bytes);
        response.fields = target.fields;
        target.listener.onResponse(response);
    }
    /** Allocating HTTP/JSON convenience API. Token supplier is consulted for every call. */
    public CompletableFuture<JsonNode> get(String pathAndQuery) {
        var uri = Environment.HTTP.resolve(pathAndQuery);
        if (!java.util.Objects.equals(uri.getScheme(), Environment.HTTP.getScheme()) || !java.util.Objects.equals(uri.getAuthority(), Environment.HTTP.getAuthority())) throw new IllegalArgumentException("HTTP request must stay on this environment");
        return tokens.get().thenCompose(token -> http.sendAsync(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(token)).GET().build(), HttpResponse.BodyHandlers.ofInputStream()))
            .thenApply(response -> {
                try (var input = response.body()) {
                    byte[] bytes = input.readNBytes(8 * 1024 * 1024 + 1);
                    if (bytes.length > 8 * 1024 * 1024) throw new IOException("HTTP response too large");
                    if (response.statusCode() < 200 || response.statusCode() >= 300) throw new RequestException("HTTP " + response.statusCode() + ": " + new String(bytes, StandardCharsets.UTF_8));
                    return Contract.JSON.readTree(bytes);
                } catch (IOException error) { throw new CompletionException(error); }
            }).toCompletableFuture();
    }
    /** Ends the client for good: the first attempt failed, re-authentication was refused, or the caller closed it. */
    private synchronized void fail(Throwable error) {
        if (closed) return;
        closed = true; generation++; authenticated = false;
        ready.completeExceptionally(error);
        Request[] failed = pending.values().toArray(Request[]::new);
        pending.clear();
        reservedWindowBytes = 0;
        for (Request request : failed) { request.closed = true; request.completion.completeExceptionally(error); }
        if (socket != null) { socket.abort(); socket = null; }
        setStatus(ConnectionState.CLOSED, attempt, null, error instanceof ClientClosed ? null : error);
        timer.shutdown();
    }
    @Override public void close() { fail(new ClientClosed()); }
    public static final class RequestException extends RuntimeException {
        public RequestException(String message) { super(message); }
    }
    private static final class AuthenticationException extends IOException {
        AuthenticationException(String message) { super(message); }
    }
    private static final class ClientClosed extends IOException {
        ClientClosed() { super("Client closed"); }
    }
}
