package com.openmdta.sdk.p_globex;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.agrona.ExpandableArrayBuffer;
import com.openmdta.sdk.p_globex.sbe.gateway_protocol.*;

/** Commands scoped to one deployed dataset. Cold request construction may allocate. */
public final class Dataset {
    public final String id;
    final byte[] dataset;
    private final byte[] quality;
    private final String qualityName;
    final Client client;
    private final JsonNode binding;
    Dataset(Client client, JsonNode binding) { this(client, binding, "RT"); }
    private Dataset(Client client, JsonNode binding, String quality) {
        this.client = client; this.binding = binding; this.id = binding.path("id").asText();
        this.dataset = id.getBytes(StandardCharsets.UTF_8); this.qualityName = quality;
        this.quality = quality.getBytes(StandardCharsets.UTF_8);
    }
    public Dataset quality(String quality) {
        if (!binding.path("qualities").has(quality)) throw new IllegalArgumentException("Unconfigured quality");
        return new Dataset(client, binding, quality);
    }
    public Request latest(MarketSelector selector, List<? extends Block<?>> blocks, MarketDataListener listener) {
        return latest(selector.expression(), blocks, listener);
    }
    public Request latest(String selector, List<? extends Block<?>> blocks, MarketDataListener listener) {
        require("latest");
        byte[] expression = selector.getBytes(StandardCharsets.UTF_8), adjustment = "raw".getBytes(StandardCharsets.UTF_8);
        var encoder = new SnapshotRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "SNAPSHOT"));
        encoder.putExpression(expression, 0, expression.length).putAdjustment(adjustment, 0, adjustment.length).putDataset(dataset, 0, dataset.length);
        return client.request("snapshot", encoder, MarketDataUpdate.listener(blocks, listener));
    }
    public Request latestStream(MarketSelector selector, List<? extends Block<?>> blocks, MarketDataListener listener) {
        return latestStream(selector.expression(), blocks, listener);
    }
    public Request latestStream(String selector, List<? extends Block<?>> blocks, MarketDataListener listener) {
        require("latest");
        byte[] expression = selector.getBytes(StandardCharsets.UTF_8), adjustment = "raw".getBytes(StandardCharsets.UTF_8);
        var encoder = new StreamRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "STREAM"));
        encoder.putExpression(expression, 0, expression.length).putAdjustment(adjustment, 0, adjustment.length).putDataset(dataset, 0, dataset.length);
        return client.request("stream", encoder, MarketDataUpdate.listener(blocks, listener));
    }
    public Request timeseries(MarketSelector selector, Instant from, Instant through, List<? extends Block<?>> blocks, MarketDataListener listener) {
        if (selector.subject() == MarketSelector.Subject.LIST) throw new IllegalArgumentException("List selectors are not supported for single-record history");
        return timeseries(selector.expression(), from, through, blocks, listener);
    }
    public Request timeseries(String selector, Instant from, Instant through, List<? extends Block<?>> blocks, MarketDataListener listener) {
        require("timeseries");
        if (through.isBefore(from)) throw new IllegalArgumentException("History end precedes start");
        byte[] expression = selector.getBytes(StandardCharsets.UTF_8), adjustment = "raw".getBytes(StandardCharsets.UTF_8);
        var encoder = new TsRawRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "TS_RAW")).from(micros(from)).through(micros(through)).maxRows(10_000);
        encoder.putExpression(expression, 0, expression.length).putQuality(quality, 0, quality.length).putAdjustment(adjustment, 0, adjustment.length).putDataset(dataset, 0, dataset.length);
        return client.request("ts-raw", encoder, MarketDataUpdate.listener(blocks, listener));
    }
    public Request streamSubscribe(List<? extends Block<?>> blocks, Request.Listener listener) {
        require("feed");
        var encoder = new FeedLiveRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "STREAM"));
        encoder.putDataset(dataset, 0, dataset.length).putQuality(quality, 0, quality.length);
        return client.request("feed-live", encoder, listener);
    }
    /** Exclusive start and inclusive end, both unsigned uint64 bit patterns in Java longs. */
    public Request streamRecover(long start, long end, List<? extends Block<?>> blocks, Request.Listener listener) {
        require("latest");
        if (Long.compareUnsigned(start, end) >= 0) throw new IllegalArgumentException("Invalid recovery range");
        var encoder = new FeedRecoveryRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "STREAM")).afterMessageId(start).throughMessageId(end);
        encoder.putDataset(dataset, 0, dataset.length).putQuality(quality, 0, quality.length);
        var batch = new MarketDataMessageBatchDecoder();
        return client.request("feed-recovery", encoder, response -> {
            if (response.templateId() == MarketDataMessageBatchDecoder.TEMPLATE_ID) {
                response.decode(batch);
                var messages = batch.messages();
                if (messages.count() < 0 || messages.count() > response.body().capacity() / 14) throw new IllegalArgumentException("Invalid recovery batch");
                while (messages.hasNext()) {
                    long id = messages.next().messageId();
                    if (Long.compareUnsigned(id, start) <= 0 || Long.compareUnsigned(id, end) > 0) throw new IllegalArgumentException("Recovery row outside requested interval");
                }
            }
            listener.onResponse(response);
        });
    }
    public Request streamSnapshot(List<? extends Block<?>> blocks, Request.Listener listener) {
        require("latest");
        var encoder = new FeedSnapshotRequestEncoder().wrap(new ExpandableArrayBuffer(), 0).blockMask(mask(blocks, "SNAPSHOT"));
        encoder.putDataset(dataset, 0, dataset.length).putQuality(quality, 0, quality.length);
        return client.request("feed-snapshot", encoder, listener);
    }
    public Request read(MarketSelector selector, List<? extends CatalogField<?>> fields, CatalogListener listener) {
        return read(List.of(selector.expression()), fields, listener);
    }
    public Request read(List<String> identifiers, List<? extends CatalogField<?>> fields, CatalogListener listener) {
        require("catalog");
        if (identifiers.isEmpty() || identifiers.size() > 256 || fields.size() > 64) throw new IllegalArgumentException("Invalid Catalog selection size");
        var encoder = new CatalogRequestEncoder().wrap(new ExpandableArrayBuffer(), 0);
        var ids = encoder.identifiersCount(identifiers.size());
        for (String identifier : identifiers) { byte[] bytes = identifier.getBytes(StandardCharsets.UTF_8); ids.next().putIdentifier(bytes, 0, bytes.length); }
        var labels = encoder.fieldsCount(fields.size());
        for (CatalogField<?> field : fields) { byte[] bytes = field.label().getBytes(StandardCharsets.UTF_8); labels.next().putLabel(bytes, 0, bytes.length); }
        encoder.putCatalog(dataset, 0, dataset.length);
        return client.request("catalog", encoder, CatalogRecord.listener(fields, dataset, false, listener));
    }
    public Request catalogSubscribe(List<? extends CatalogField<?>> fields, CatalogListener listener) {
        return catalogSubscribe(fields, null, listener);
    }
    /** Opaque resume cursor; the server may instead deliver a new snapshot. */
    public Request catalogSubscribe(List<? extends CatalogField<?>> fields, String cursor, CatalogListener listener) {
        require("catalog");
        byte[] resume = cursor == null ? new byte[0] : cursor.getBytes(StandardCharsets.UTF_8);
        if (fields.size() > 64 || resume.length > 4096) throw new IllegalArgumentException("Invalid Catalog selection or cursor");
        var encoder = new CatalogFeedRequestEncoder().wrap(new ExpandableArrayBuffer(), 0);
        var labels = encoder.fieldsCount(fields.size());
        for (CatalogField<?> field : fields) { byte[] bytes = field.label().getBytes(StandardCharsets.UTF_8); labels.next().putLabel(bytes, 0, bytes.length); }
        encoder.putCatalog(dataset, 0, dataset.length).putCursor(resume, 0, resume.length);
        return client.request("catalog-feed", encoder, CatalogRecord.listener(fields, dataset, true, listener));
    }
    public Request metadata(Request.Listener listener) {
        if (!binding.path("qualities").has(qualityName)) throw new IllegalArgumentException("Unconfigured quality");
        var encoder = new StreamMetadataQueryEncoder().wrap(new ExpandableArrayBuffer(), 0);
        encoder.putDataset(dataset, 0, dataset.length).putQuality(quality, 0, quality.length);
        return client.request("stream-metadata", encoder, listener);
    }
    public Feeds.Stream streamFeed(List<? extends Block<?>> blocks, Feeds.StreamSink sink) {
        return new Feeds.Stream(this, List.copyOf(blocks), sink);
    }
    public Feeds.Catalog catalogFeed(List<? extends CatalogField<?>> fields, Feeds.CatalogSink sink) {
        return new Feeds.Catalog(this, List.copyOf(fields), sink);
    }
    public Feeds.MemoryStream streamFeedMemory(List<? extends Block<?>> blocks) {
        Feeds.MemoryStream memory = new Feeds.MemoryStream();
        memory.handle = streamFeed(blocks, memory); return memory;
    }
    public Feeds.MemoryCatalog catalogFeedMemory(List<? extends CatalogField<?>> fields) {
        Feeds.MemoryCatalog memory = new Feeds.MemoryCatalog();
        memory.handle = catalogFeed(fields, memory); return memory;
    }
    private void require(String capability) {
        if (capability.equals("feed") || capability.equals("latest") || capability.equals("timeseries")) {
            if (!binding.path("qualities").path(qualityName).path(capability).asBoolean()) throw new IllegalArgumentException("Dataset does not expose " + capability + " at " + qualityName);
        } else {
            for (JsonNode value : binding.path("capabilities")) if (value.asText().equals(capability)) return;
            throw new IllegalArgumentException("Dataset does not expose " + capability);
        }
    }
    private long mask(List<? extends Block<?>> blocks, String command) {
        if (blocks.isEmpty()) throw new IllegalArgumentException("Select at least one block");
        long result = 0;
        for (Block<?> block : blocks) {
            if (!binding.path("fields").has(block.semantic()) || !block.commands().contains(command)) throw new IllegalArgumentException("Unsupported block " + block.semantic() + " for " + command);
            result |= 1L << block.format().templateId();
        }
        return result;
    }
    private static long micros(Instant instant) {
        if (instant.getEpochSecond() < 0) throw new IllegalArgumentException("Time precedes Unix epoch");
        return Math.addExact(Math.multiplyExact(instant.getEpochSecond(), 1_000_000L), instant.getNano() / 1000);
    }
}
