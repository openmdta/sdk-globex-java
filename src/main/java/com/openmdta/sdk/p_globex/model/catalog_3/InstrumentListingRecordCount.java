package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record InstrumentListingRecordCount(long count) {
    public static InstrumentListingRecordCount decode(com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentListingRecordCountDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        long result0 = decoder.count();
        return new InstrumentListingRecordCount(result0);
    }
}

