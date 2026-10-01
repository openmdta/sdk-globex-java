package com.openmdta.sdk.p_globex;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/** Borrowed for one callback only. Reused on the next delivery. Never retain it. */
final class Response {
    private int schemaId, templateId, version, blockLength;
    private final UnsafeBuffer body = new UnsafeBuffer(0, 0);
    /** Field IDs announced for the owning request; market-data batches resolve through it. */
    AnnouncedFields fields;

    void wrap(int schema, int template, int actingVersion, int fixedLength, DirectBuffer buffer) {
        buffer.boundsCheck(0, fixedLength);
        schemaId = schema; templateId = template; version = actingVersion; blockLength = fixedLength;
        body.wrap(buffer);
    }
    public int schemaId() { return schemaId; }
    public int templateId() { return templateId; }
    public int version() { return version; }
    public int blockLength() { return blockLength; }
    public DirectBuffer body() { return body; }

    public <D extends MessageDecoderFlyweight> D decode(D target) {
        if (target.sbeSchemaId() != schemaId || target.sbeTemplateId() != templateId) throw new IllegalArgumentException("Decoder does not match response");
        target.wrap(body, 0, blockLength, version);
        return target;
    }

    /** Explicit ownership transfer: copies the body and allocates an owned response with its format. */
    public Owned copy() {
        byte[] bytes = new byte[body.capacity()];
        body.getBytes(0, bytes);
        return new Owned(new Format(schemaId, templateId, version, blockLength), bytes, fields);
    }

    /** The byte array belongs to the caller and may be persisted or sent to another thread. */
    public record Owned(Format format, byte[] bytes, AnnouncedFields fields) {
        public Response view() {
            Response result = new Response();
            result.wrap(format.schemaId(), format.templateId(), format.version(), format.blockLength(), new UnsafeBuffer(bytes));
            result.fields = fields;
            return result;
        }
    }
}
