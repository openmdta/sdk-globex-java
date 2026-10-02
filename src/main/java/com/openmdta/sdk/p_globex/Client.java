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
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.agrona.DirectBuffer;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageEncoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.DatasetFieldsDecoder;
import com.openmdta.sdk.p_globex.sbe.sbe_websocket.*;

/** One authenticated connection. Callbacks borrow transport bytes and run serially. */
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
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "openmdta-heartbeat"); t.setDaemon(true); return t; });
    private final CompletableFuture<Client> ready = new CompletableFuture<>();
    private final UnsafeBuffer incoming = new UnsafeBuffer(0, 0), body = new UnsafeBuffer(0, 0), errorText = new UnsafeBuffer(0, 0);
    private final MessageHeaderDecoder header = new MessageHeaderDecoder();
    private final ResponseDecoder envelope = new ResponseDecoder();
    private final Response response = new Response();
    private ByteBuffer fragments;
    private WebSocket socket;
    boolean closed;
    private long sequence = 2, lastHeartbeat = System.nanoTime();
    private CompletableFuture<Void> sending = CompletableFuture.completedFuture(null);
    private int queuedSendBytes;

    private final WebSocket.Listener transport = new WebSocket.Listener() {
        @Override public void onOpen(WebSocket webSocket) {
            synchronized (Client.this) {
                socket = webSocket;
                if (closed) socket.abort(); else socket.request(1);
            }
        }
        @Override public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            synchronized (Client.this) {
                if (closed) return CONSUMED;
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
                } catch (Throwable error) { fail(error); }
                if (!closed) webSocket.request(1);
                return CONSUMED;
            }
        }
        @Override public CompletionStage<?> onText(WebSocket socket, CharSequence text, boolean last) {
            fail(new IOException("Unexpected WebSocket text message"));
            return CONSUMED;
        }
        @Override public CompletionStage<?> onClose(WebSocket socket, int code, String reason) {
            fail(new IOException("Gateway disconnected (" + code + "): " + reason));
            return CONSUMED;
        }
        @Override public void onError(WebSocket socket, Throwable error) { fail(error); }
    };

    private Client(HttpClient http, Supplier<? extends CompletionStage<byte[]>> tokens) {
        this.http = java.util.Objects.requireNonNull(http);
        this.tokens = java.util.Objects.requireNonNull(tokens);
    }
    public static CompletableFuture<Client> connect(byte[] token) {
        byte[] copy = token.clone();
        return connect(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), () -> CompletableFuture.completedFuture(copy));
    }
    public static CompletableFuture<Client> connect(HttpClient http, Supplier<? extends CompletionStage<byte[]>> tokens) {
        Client client = new Client(http, tokens);
        try {
            tokens.get().thenCompose(bytes -> {
                if (bytes == null || bytes.length == 0 || bytes.length > 64 * 1024) throw new IllegalArgumentException("Invalid token length");
                return http.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10)).subprotocols(WINDOW_PROTOCOL).buildAsync(Environment.WEBSOCKET, client.transport).thenAccept(socket -> {
                    if (!WINDOW_PROTOCOL.equals(socket.getSubprotocol())) throw new IllegalStateException("Gateway did not negotiate the SBE session protocol");
                    UnsafeBuffer buffer = new UnsafeBuffer(new byte[20 + bytes.length]);
                    AuthRequestEncoder auth = new AuthRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder());
                    auth.requestId(1).putToken(bytes, 0, bytes.length);
                    client.send(buffer.byteArray());
                });
            }).whenComplete((ignored, error) -> { if (error != null) client.fail(error); });
        } catch (Throwable error) { client.fail(error); }
        client.ready.orTimeout(15, TimeUnit.SECONDS).whenComplete((ignored, error) -> { if (error != null) client.fail(error); });
        synchronized (client) {
        if (!client.closed) client.timer.scheduleAtFixedRate(() -> {
            synchronized (client) {
                long timeout = Contract.ROOT.path("runtime").path("limits").path("heartbeatTimeoutMillis").asLong(60_000);
                if (!client.closed && TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - client.lastHeartbeat) > timeout) client.fail(new IOException("Gateway heartbeat timed out"));
            }
        }, 1, 1, TimeUnit.SECONDS);
        }
        return client.ready;
    }
    public Dataset datasetGlobex() {
        return dataset("globex");
    }
    public Dataset datasetLus() {
        return dataset("lus").quality("DL");
    }
    public Dataset datasetSim() {
        return dataset("sim");
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
        request.windowed = operation.startsWith("feed-") || operation.equals("catalog-feed");
        if (request.windowed) {
            if ((long) reservedWindowBytes + WINDOW_BYTES > 64 * 1024 * 1024) throw new IllegalStateException("Connection feed buffer capacity reached");
            reservedWindowBytes += WINDOW_BYTES;
        }
        listener.onRegistered(request);
        pending.put(id, request);
        send(buffer.byteArray());
        if (request.windowed) {
            UnsafeBuffer window = new UnsafeBuffer(new byte[28]);
            new ResponseWindowEncoder().wrapAndApplyHeader(window, 0, new MessageHeaderEncoder())
                .targetId(id).responses(16).bytes(WINDOW_BYTES).maxBodyBytes(Math.min(MAX_BODY, MAX_FRAME - 128));
            send(window.byteArray());
        } else credit(id, CREDITS);
        return request;
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
        if (removed != null && !closed) {
            UnsafeBuffer buffer = new UnsafeBuffer(new byte[24]);
            new CancelRequestEncoder().wrapAndApplyHeader(buffer, 0, new MessageHeaderEncoder()).requestId(sequence++).targetId(request.id);
            send(buffer.byteArray());
        }
        request.completion.cancel(false);
    }
    private synchronized void send(byte[] bytes) {
        if (closed) return;
        // These unchanged templates retain their v0 acting version even when
        // generated from the extended session schema, including v1 fallback.
        if (bytes.length >= 8 && bytes[2] >= 1 && bytes[2] <= 4 && bytes[3] == 0) { bytes[6] = 0; bytes[7] = 0; }
        if ((long) queuedSendBytes + bytes.length > 16 * 1024 * 1024) { fail(new IOException("Outgoing queue limit exceeded")); return; }
        queuedSendBytes += bytes.length;
        sending = sending.thenCompose(ignored -> socket.sendBinary(ByteBuffer.wrap(bytes), true)).thenAccept(ignored -> {});
        sending.whenComplete((ignored, error) -> {
            synchronized (this) { queuedSendBytes -= bytes.length; if (error != null) fail(error); }
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
            if (status != Status.CONTINUE || body.capacity() != 0) throw new IOException("Authentication failed: " + errorText.getStringWithoutLengthUtf8(0, errorText.capacity()));
            lastHeartbeat = System.nanoTime(); ready.complete(this); return;
        }
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
    private synchronized void fail(Throwable error) {
        if (closed) return;
        closed = true; ready.completeExceptionally(error);
        Request[] failed = pending.values().toArray(Request[]::new);
        pending.clear();
        reservedWindowBytes = 0;
        for (Request request : failed) { request.closed = true; request.completion.completeExceptionally(error); }
        if (socket != null) socket.abort();
        timer.shutdownNow();
    }
    @Override public void close() { fail(new IOException("Client closed")); }
    public static final class RequestException extends RuntimeException {
        public RequestException(String message) { super(message); }
    }
}
