package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record FeedPartition(String partition_id) {
    public static FeedPartition decode(com.openmdta.sdk.p_globex.sbe.catalog_3.FeedPartitionDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.partition_id();
        return new FeedPartition(result0);
    }
}

