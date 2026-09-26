package com.openmdta.sdk.p_globex;

import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.CatalogRecordDecoder;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.CatalogFeedControlDecoder;

/** Borrowed whole-record replacement, or deletion when exists() is false. */
public final class CatalogRecord {
    public enum Listing { NOT_LISTED, LISTED }
    public enum Activity { UNKNOWN, ACTIVE, INACTIVE }
    @FunctionalInterface public interface ValueConsumer<D> { void accept(DirectBuffer subfield, D value); }
    private final CatalogRecordDecoder record = new CatalogRecordDecoder();
    private final UnsafeBuffer catalog = new UnsafeBuffer(0, 0), identifier = new UnsafeBuffer(0, 0), key = new UnsafeBuffer(0, 0);
    private final IdentityHashMap<CatalogField<?>, Selected<?>> selected = new IdentityHashMap<>();
    private Response response;

    private CatalogRecord(List<? extends CatalogField<?>> fields) {
        for (CatalogField<?> field : fields.isEmpty() ? Fields.ALL : fields) {
            if (selected.put(field, new Selected<>(field)) != null) throw new IllegalArgumentException("Duplicate Catalog field");
        }
    }
    static Request.Listener listener(List<? extends CatalogField<?>> fields, byte[] dataset, boolean subscription, CatalogListener listener) {
        Objects.requireNonNull(listener);
        CatalogRecord view = new CatalogRecord(fields);
        var control = new CatalogFeedControlDecoder();
        var json = new UnsafeBuffer(0, 0);
        return new Request.Listener() {
            private boolean staging;
            @Override public void onResponse(Response response) throws Exception {
                if (response.templateId() == CatalogRecordDecoder.TEMPLATE_ID) {
                    view.response = response;
                    response.decode(view.record);
                    if (view.record.exists() > 1 || view.record.lifecyclePresent() > 1 || view.record.visible() > 1
                            || view.record.lifecyclePresent() == 1 && (view.record.listing() > 1 || view.record.activity() > 2
                            || view.record.listing() == 0 && view.record.activity() != 0)) throw new IllegalArgumentException("Invalid Catalog lifecycle");
                    int phase = view.record.phase().value();
                    if (phase != 1 && phase != 2 || subscription && (phase == 1) != staging) throw new IllegalArgumentException("Unexpected Catalog record phase");
                    var entries = view.record.fields();
                    if (entries.count() < 0 || entries.actingBlockLength() < 6 || entries.count() > response.body().capacity() / entries.actingBlockLength()) {
                        throw new IllegalArgumentException("Invalid Catalog field group");
                    }
                    while (entries.hasNext()) { entries.next(); entries.skipLabel(); entries.skipSubfield(); entries.skipPayload(); }
                    view.record.wrapCatalog(view.catalog); view.record.wrapIdentifier(view.identifier); view.record.wrapRecordIdentifier(view.key);
                    if (view.record.limit() != response.body().capacity() || view.catalog.capacity() != dataset.length) throw new IllegalArgumentException("Invalid Catalog record");
                    for (int i = 0; i < dataset.length; i++) if (view.catalog.getByte(i) != dataset[i]) throw new IllegalArgumentException("Unexpected Catalog dataset");
                    listener.onRecord(view);
                    return;
                }
                if (!subscription) throw new IllegalArgumentException("Unexpected Catalog read response");
                response.decode(control); control.wrapJson(json);
                if (control.limit() != response.body().capacity()) throw new IllegalArgumentException("Invalid Catalog control length");
                var value = Contract.JSON.readTree(json.getStringWithoutLengthUtf8(0, json.capacity()));
                switch (value.path("kind").asText()) {
                    case "snapshot_begin" -> {
                        if (staging) throw new IllegalStateException("Nested Catalog snapshot");
                        staging = true; listener.onSnapshotBegin();
                    }
                    case "snapshot_complete", "cursor" -> {
                        boolean complete = value.path("kind").asText().equals("snapshot_complete");
                        if (complete != staging || !value.path("cursor").isTextual() || value.path("cursor").asText().isEmpty()) {
                            throw new IllegalArgumentException("Unexpected Catalog cursor control");
                        }
                        String cursor = value.path("cursor").asText();
                        if (complete) { listener.onSnapshotComplete(cursor); staging = false; }
                        else listener.onCursor(cursor);
                    }
                    default -> throw new IllegalArgumentException("Unknown Catalog control");
                }
            }
        };
    }
    /** Generated decoder for a single-valued field, or null if absent/deleted. */
    public <D extends MessageDecoderFlyweight> D value(CatalogField<D> field) {
        Selected<D> selection = selected(field);
        selection.single = null;
        forEachValue(field, selection.readSingle);
        return selection.single;
    }
    /** Handles repeated/subfield values without allocating a map or a decoder for each entry. */
    public <D extends MessageDecoderFlyweight> void forEachValue(CatalogField<D> field, ValueConsumer<D> consumer) {
        Objects.requireNonNull(consumer);
        Selected<D> selection = selected(field);
        if (!exists()) return;
        response.decode(selection.scan);
        var entries = selection.scan.fields();
        while (entries.hasNext()) {
            entries.next(); entries.wrapLabel(selection.label); entries.wrapSubfield(selection.subfield); entries.wrapPayload(selection.body);
            if (selection.label.capacity() != selection.name.length) continue;
            boolean matches = true;
            for (int i = 0; i < selection.name.length; i++) if (selection.label.getByte(i) != selection.name[i]) { matches = false; break; }
            if (matches) {
                selection.payload.wrap(selection.body);
                field.wrap(selection.decoder, selection.payload);
                consumer.accept(selection.subfield, selection.decoder);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private <D extends MessageDecoderFlyweight> Selected<D> selected(CatalogField<D> field) {
        Selected<?> value = selected.get(field);
        if (value == null) throw new IllegalArgumentException("Catalog field was not selected by this request");
        return (Selected<D>) value; // Identity lookup pairs the descriptor with its own decoder supplier.
    }
    private static final class Selected<D extends MessageDecoderFlyweight> {
        final byte[] name;
        final D decoder;
        final CatalogRecordDecoder scan = new CatalogRecordDecoder();
        final UnsafeBuffer label = new UnsafeBuffer(0, 0), subfield = new UnsafeBuffer(0, 0), body = new UnsafeBuffer(0, 0), payload = new UnsafeBuffer(0, 0);
        final ValueConsumer<D> readSingle;
        D single;
        Selected(CatalogField<D> field) {
            name = field.label().getBytes(StandardCharsets.UTF_8); decoder = field.decoder().get();
            readSingle = (key, value) -> {
                if (key.capacity() != 0 || single != null) throw new IllegalStateException("Use forEachValue for a repeated Catalog field");
                single = value;
            };
        }
    }
    public boolean exists() { return record.exists() == 1; }
    public boolean snapshot() { return record.phase().value() == 1; }
    public boolean hasLifecycle() { return record.lifecyclePresent() == 1; }
    public Listing listing() { return !hasLifecycle() ? null : record.listing() == 1 ? Listing.LISTED : Listing.NOT_LISTED; }
    public Activity activity() { return listing() != Listing.LISTED ? null : switch (record.activity()) { case 1 -> Activity.ACTIVE; case 2 -> Activity.INACTIVE; default -> Activity.UNKNOWN; }; }
    public boolean visible() { return hasLifecycle() && record.visible() == 1; }
    public long effectiveTimeMicros() { return record.effectiveTimeMicros(); }
    /** -1 is the unsigned uint64 null sentinel. */
    public long hideAtMicros() { return record.hideAtMicros(); }
    /** -1 is the unsigned uint64 null sentinel. */
    public long hiddenAtUnixSeconds() { return record.hiddenAtUnixSeconds(); }
    public DirectBuffer recordKeyBytes() { return key; }
    public DirectBuffer datasetBytes() { return catalog; }
    public DirectBuffer identifierBytes() { return identifier; }
    /** Allocating convenience accessor. */
    public String recordKey() { return key.getStringWithoutLengthUtf8(0, key.capacity()); }
    /** Explicit allocating copy for storage or another thread. */
    public Response.Owned copy() { return response.copy(); }
}
