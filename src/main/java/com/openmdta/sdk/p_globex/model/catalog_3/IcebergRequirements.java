package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record IcebergRequirements(String minimum_iceberg_total_volume, String minimum_iceberg_display_volume) {
    public static IcebergRequirements decode(com.openmdta.sdk.p_globex.sbe.catalog_3.IcebergRequirementsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.minimum_iceberg_total_volume();
        String result1 = decoder.minimum_iceberg_display_volume();
        return new IcebergRequirements(result0, result1);
    }
}

