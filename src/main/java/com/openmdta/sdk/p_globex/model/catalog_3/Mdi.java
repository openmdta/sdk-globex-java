package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record Mdi(String mdi_address_a_netted, String mdi_port_a_netted, String mdi_address_b_netted, String mdi_port_b_netted, String mdi_market_depth_netted, String mdi_market_depth_time_interval_netted, String mdi_recovery_time_interval_netted) {
    public static Mdi decode(com.openmdta.sdk.p_globex.sbe.catalog_3.MdiDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.mdi_address_a_netted();
        String result1 = decoder.mdi_port_a_netted();
        String result2 = decoder.mdi_address_b_netted();
        String result3 = decoder.mdi_port_b_netted();
        String result4 = decoder.mdi_market_depth_netted();
        String result5 = decoder.mdi_market_depth_time_interval_netted();
        String result6 = decoder.mdi_recovery_time_interval_netted();
        return new Mdi(result0, result1, result2, result3, result4, result5, result6);
    }
}

