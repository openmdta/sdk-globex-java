package com.openmdta.sdk.p_globex;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.MarketDataMessageBatchDecoder;

/** Reusable zero-copy batch, including message boundaries and headerless owner payloads. */
final class BatchView {
    @FunctionalInterface interface FieldConsumer { void accept(Field field) throws Exception; }
    @FunctionalInterface interface MessageConsumer { void accept(Message message) throws Exception; }
    Response response;
    private final MarketDataMessageBatchDecoder decoder = new MarketDataMessageBatchDecoder();
    private final MarketDataMessageBatchDecoder messages = new MarketDataMessageBatchDecoder();
    private final UnsafeBuffer key = new UnsafeBuffer(0, 0), dataset = new UnsafeBuffer(0, 0), payload = new UnsafeBuffer(0, 0);
    private final Field field = new Field();
    private final Message message = new Message();
    private int fieldsOffset, gapsOffset, fieldCount, messageCount;

    public BatchView wrap(Response response) {
        this.response = response;
        response.decode(decoder);
        if (decoder.phase().value() != 1 && decoder.phase().value() != 2) throw new IllegalArgumentException("Invalid batch phase");
        response.decode(messages);
        var group = decoder.messages();
        messageCount = group.count();
        if (messageCount < 0 || group.actingBlockLength() < 14 || messageCount > response.body().capacity() / group.actingBlockLength()) throw new IllegalArgumentException("Invalid message group");
        int next = 0;
        while (group.hasNext()) {
            group.next();
            if (group.firstField() != next) throw new IllegalArgumentException("Non-contiguous message boundary");
            next = Math.addExact(next, group.fieldCount());
        }
        fieldsOffset = decoder.limit();
        var values = decoder.fields();
        fieldCount = values.count();
        if (fieldCount != next || fieldCount < 0 || values.actingBlockLength() < 23 || fieldCount > response.body().capacity() / values.actingBlockLength()) throw new IllegalArgumentException("Invalid field group");
        while (values.hasNext()) { values.next(); response.body().boundsCheck(decoder.limit() - values.actingBlockLength(), values.actingBlockLength()); }
        gapsOffset = decoder.limit();
        var gaps = decoder.gaps();
        if (gaps.count() < 0 || gaps.actingBlockLength() < 16 || gaps.count() > response.body().capacity() / gaps.actingBlockLength()) throw new IllegalArgumentException("Invalid gap group");
        while (gaps.hasNext()) { gaps.next(); response.body().boundsCheck(decoder.limit() - gaps.actingBlockLength(), gaps.actingBlockLength()); }
        decoder.wrapDatasetRecordKey(key); decoder.wrapDataset(dataset); decoder.wrapPayload(payload);
        if (decoder.limit() != response.body().capacity()) throw new IllegalArgumentException("Trailing or truncated batch bytes");
        return this;
    }
    public int messageCount() { return messageCount; }
    public int fieldCount() { return fieldCount; }
    public boolean snapshot() { return decoder.phase().value() == 1; }
    public DirectBuffer recordKeyBytes() { return key; }
    public DirectBuffer datasetBytes() { return dataset; }
    /** Allocating UTF-8 convenience accessor; use recordKeyBytes on the hot path. */
    public String recordKey() { return key.getStringWithoutLengthUtf8(0, key.capacity()); }

    public MarketDataMessageBatchDecoder.GapsDecoder gaps() {
        decoder.limit(gapsOffset);
        return decoder.gaps();
    }

    /** One callback per source message, including messages with no selected fields. */
    void forEachMessage(MessageConsumer consumer) throws Exception {
        messages.sbeRewind();
        var rows = messages.messages();
        while (rows.hasNext()) {
            rows.next();
            message.id = rows.messageId();
            message.firstField = Math.toIntExact(rows.firstField());
            message.count = rows.fieldCount();
            consumer.accept(message);
        }
    }

    /** Borrowed message and fields; fields can only be visited within their owning message. */
    final class Message {
        private long id;
        private int firstField, count;
        long messageId() { return id; }
        boolean snapshot() { return BatchView.this.snapshot(); }
        DirectBuffer recordKeyBytes() { return key; }
        DirectBuffer datasetBytes() { return dataset; }
        void forEachField(FieldConsumer consumer) throws Exception {
            decoder.limit(fieldsOffset);
            var values = decoder.fields();
            decoder.limit(Math.addExact(decoder.limit(), Math.multiplyExact(firstField, values.actingBlockLength())));
            for (int i = 0; i < count; i++) {
                values.next();
                int offset = Math.toIntExact(values.payloadOffset()), length = Math.toIntExact(values.payloadLength());
                payload.boundsCheck(offset, length);
                if (values.clear() > 1 || values.clear() == 1 && length != 0 || values.clear() == 0 && length < values.blockLength()) throw new IllegalArgumentException("Invalid field payload");
                field.value = values; field.fields = response.fields; field.body.wrap(payload, offset, length);
                consumer.accept(field);
            }
        }
    }

    void requireRange(long after, long through) {
        messages.sbeRewind();
        var rows = messages.messages();
        while (rows.hasNext()) {
            long id = rows.next().messageId();
            if (Long.compareUnsigned(id, after) <= 0 || Long.compareUnsigned(id, through) > 0) {
                throw new IllegalArgumentException("Recovery row outside requested interval");
            }
        }
    }

    public static final class Field {
        private MarketDataMessageBatchDecoder.FieldsDecoder value;
        private AnnouncedFields fields;
        private final UnsafeBuffer body = new UnsafeBuffer(0, 0);
        /** Dataset field ID; its semantic field and layout were announced once for the request. */
        public int fieldId() { return value.fieldId(); }
        /** Semantic field, such as {@code openmdta::BidAsk}. */
        public String semantic() { return fields.semantic(value.fieldId()); }
        /** SBE message name of the payload layout; a {@link Block} decodes exactly one layout. */
        public String layout() { return fields.layout(value.fieldId()); }
        public int version() { return value.version(); }
        public int blockLength() { return value.blockLength(); }
        public long eventTimeMicros() { return value.eventTimeMicros(); }
        public boolean clear() { return value.clear() == 1; }
        public DirectBuffer payload() { return body; }
        public <D extends MessageDecoderFlyweight> D decode(Block<D> block, D target) {
            if (clear() || !block.semantic().equals(semantic()) || !block.layout().equals(layout())) throw new IllegalArgumentException("Clear field or mismatched decoder");
            target.wrap(body, 0, blockLength(), version());
            return target;
        }
    }
}
