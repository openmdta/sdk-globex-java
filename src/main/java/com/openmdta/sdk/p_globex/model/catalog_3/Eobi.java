package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Eobi(String eobi_incremental_a, String eobi_incremental_port_a, String eobi_incremental_b, String eobi_incremental_port_b, String eobi_snapshot_a, String eobi_snapshot_port_a, String eobi_snapshot_b, String eobi_snapshot_port_b) {
    public static Eobi decode(com.openmdta.sdk.p_globex.sbe.catalog_3.EobiDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.eobi_incremental_a();
        String result1 = decoder.eobi_incremental_port_a();
        String result2 = decoder.eobi_incremental_b();
        String result3 = decoder.eobi_incremental_port_b();
        String result4 = decoder.eobi_snapshot_a();
        String result5 = decoder.eobi_snapshot_port_a();
        String result6 = decoder.eobi_snapshot_b();
        String result7 = decoder.eobi_snapshot_port_b();
        return new Eobi(result0, result1, result2, result3, result4, result5, result6, result7);
    }
}

