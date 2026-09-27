package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record VolatilityInterruptionLimits(String fixed_abs_vola_interruption_limit, String float_abs_vola_interruption_limit, String fixed_pct_vola_interruption_limit, String float_pct_vola_interruption_limit) {
    public static VolatilityInterruptionLimits decode(com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityInterruptionLimitsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.fixed_abs_vola_interruption_limit();
        String result1 = decoder.float_abs_vola_interruption_limit();
        String result2 = decoder.fixed_pct_vola_interruption_limit();
        String result3 = decoder.float_pct_vola_interruption_limit();
        return new VolatilityInterruptionLimits(result0, result1, result2, result3);
    }
}

