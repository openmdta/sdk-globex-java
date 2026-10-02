package com.openmdta.sdk.p_globex;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.DatasetFieldsDecoder;

/**
 * Field IDs the gateway named for one request. Batches carry only the dataset field ID; each
 * DatasetFields response names an ID's semantic field and payload layout once. The names are
 * decoded when the announcement arrives; lookups never allocate.
 */
final class AnnouncedFields {
    private int count;
    private int[] ids = new int[16];
    private String[] semantics = new String[16], layouts = new String[16];
    private final DatasetFieldsDecoder decoder = new DatasetFieldsDecoder();
    private final UnsafeBuffer text = new UnsafeBuffer(0, 0);

    /** A replayed request announces its fields again. */
    void clear() { count = 0; }

    void absorb(int version, int blockLength, DirectBuffer body) {
        decoder.wrap(body, 0, blockLength, version);
        var fields = decoder.fields();
        while (fields.hasNext()) {
            fields.next();
            int fieldId = fields.fieldId();
            fields.wrapSemantic(text);
            String semantic = text.getStringWithoutLengthUtf8(0, text.capacity());
            fields.wrapLayout(text);
            put(fieldId, semantic, text.getStringWithoutLengthUtf8(0, text.capacity()));
        }
    }

    void put(int fieldId, String semantic, String layout) {
        int slot = slot(fieldId);
        if (slot < 0) {
            if (count == ids.length) {
                ids = java.util.Arrays.copyOf(ids, count * 2);
                semantics = java.util.Arrays.copyOf(semantics, count * 2);
                layouts = java.util.Arrays.copyOf(layouts, count * 2);
            }
            slot = count++;
            ids[slot] = fieldId;
        }
        semantics[slot] = semantic; layouts[slot] = layout;
    }

    String semantic(int fieldId) { return semantics[required(fieldId)]; }
    String layout(int fieldId) { return layouts[required(fieldId)]; }

    private int required(int fieldId) {
        int slot = slot(fieldId);
        if (slot < 0) throw new IllegalArgumentException("Market-data field " + fieldId + " was not announced");
        return slot;
    }

    private int slot(int fieldId) {
        for (int slot = 0; slot < count; slot++) if (ids[slot] == fieldId) return slot;
        return -1;
    }
}
