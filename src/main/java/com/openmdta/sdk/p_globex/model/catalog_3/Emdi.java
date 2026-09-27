package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Emdi(String emdi_incremental_a_unnetted, String emdi_incremental_a_unnetted_port, String emdi_incremental_b_unnetted, String emdi_incremental_b_unnetted_port, String emdi_snapshot_a_unnetted, String emdi_snapshot_a_unnetted_port, String emdi_snapshot_b_unnetted, String emdi_snapshot_b_unnetted_port, String emdi_market_depth_unnetted, String emdi_snapshot_recovery_time_interval_unnetted) {
    public static Emdi decode(com.openmdta.sdk.p_globex.sbe.catalog_3.EmdiDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.emdi_incremental_a_unnetted();
        String result1 = decoder.emdi_incremental_a_unnetted_port();
        String result2 = decoder.emdi_incremental_b_unnetted();
        String result3 = decoder.emdi_incremental_b_unnetted_port();
        String result4 = decoder.emdi_snapshot_a_unnetted();
        String result5 = decoder.emdi_snapshot_a_unnetted_port();
        String result6 = decoder.emdi_snapshot_b_unnetted();
        String result7 = decoder.emdi_snapshot_b_unnetted_port();
        String result8 = decoder.emdi_market_depth_unnetted();
        String result9 = decoder.emdi_snapshot_recovery_time_interval_unnetted();
        return new Emdi(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9);
    }
}

