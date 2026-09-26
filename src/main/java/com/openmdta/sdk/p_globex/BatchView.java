package com.openmdta.sdk.p_globex;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.MarketDataMessageBatchDecoder;

/** Reusable zero-copy batch, including message boundaries and headerless owner payloads. */
public final class BatchView {
    @FunctionalInterface public interface FieldConsumer { void accept(long messageId, Field field); }
    private final MarketDataMessageBatchDecoder decoder = new MarketDataMessageBatchDecoder();
    private final MarketDataMessageBatchDecoder messages = new MarketDataMessageBatchDecoder();
    private final UnsafeBuffer key = new UnsafeBuffer(0, 0), dataset = new UnsafeBuffer(0, 0), payload = new UnsafeBuffer(0, 0);
    private final Field field = new Field();
    private int fieldsOffset, gapsOffset, fieldCount, messageCount;

    public BatchView wrap(Response response) {
        response.decode(decoder);
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
        if (fieldCount != next || fieldCount < 0 || values.actingBlockLength() < 25 || fieldCount > response.body().capacity() / values.actingBlockLength()) throw new IllegalArgumentException("Invalid field group");
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

    /** Both Field and its payload buffer are reused. The callback must finish reading before returning. */
    public void forEachField(FieldConsumer consumer) {
        forEachField(consumer, null);
    }

    /** messageComplete runs after every source message, including messages without fields. */
    public void forEachField(FieldConsumer consumer, java.util.function.LongConsumer messageComplete) {
        messages.sbeRewind();
        var rows = messages.messages();
        decoder.limit(fieldsOffset);
        var values = decoder.fields();
        while (rows.hasNext()) {
            rows.next();
            for (int i = 0; i < rows.fieldCount(); i++) {
                values.next();
                int offset = Math.toIntExact(values.payloadOffset()), length = Math.toIntExact(values.payloadLength());
                payload.boundsCheck(offset, length);
                if (values.clear() > 1 || values.clear() == 1 && length != 0 || values.clear() == 0 && length < values.blockLength()) throw new IllegalArgumentException("Invalid field payload");
                field.value = values; field.body.wrap(payload, offset, length);
                consumer.accept(rows.messageId(), field);
            }
            if (messageComplete != null) messageComplete.accept(rows.messageId());
        }
    }

    public static final class Field {
        private MarketDataMessageBatchDecoder.FieldsDecoder value;
        private final UnsafeBuffer body = new UnsafeBuffer(0, 0);
        public int schemaId() { return value.schemaId(); }
        public int templateId() { return value.templateId(); }
        public int version() { return value.version(); }
        public int blockLength() { return value.blockLength(); }
        public long eventTimeMicros() { return value.eventTimeMicros(); }
        public boolean clear() { return value.clear() == 1; }
        public DirectBuffer payload() { return body; }
        public <D extends MessageDecoderFlyweight> D decode(Block<D> block, D target) {
            if (clear() || block.format().schemaId() != schemaId() || block.format().templateId() != templateId()) throw new IllegalArgumentException("Clear field or mismatched decoder");
            target.wrap(body, 0, blockLength(), version());
            return target;
        }
    }
}
