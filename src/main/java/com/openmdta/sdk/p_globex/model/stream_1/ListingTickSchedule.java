package com.openmdta.sdk.p_globex.model.stream_1;

/** Immutable owned value; decoding allocates. */
public record ListingTickSchedule(java.util.List<Tiers> tiers, java.util.List<Byte> band) {
    public ListingTickSchedule {
        tiers = java.util.List.copyOf(tiers);
        band = java.util.List.copyOf(band);
    }
    public static ListingTickSchedule decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingTickScheduleDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Tiers>();
        var value0Group = decoder.tiers();
        while (value0Group.hasNext()) {
            value0.add(Tiers.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Tiers> result0 = java.util.List.copyOf(value0);
        byte[] value1Bytes = new byte[decoder.bandLength()];
        decoder.getBand(value1Bytes, 0, value1Bytes.length);
        var value1 = new java.util.ArrayList<Byte>(value1Bytes.length);
        for (byte item : value1Bytes) { value1.add(item); }
        java.util.List<Byte> result1 = java.util.List.copyOf(value1);
        return new ListingTickSchedule(result0, result1);
    }
public record Tiers(java.math.BigDecimal upperBound, java.math.BigDecimal increment) {
    public static Tiers decode(com.openmdta.sdk.p_globex.sbe.stream_1.ListingTickScheduleDecoder.TiersDecoder decoder, int actingVersion) {
        java.math.BigDecimal result0 = java.math.BigDecimal.valueOf(decoder.upperBound().mantissa(), -decoder.upperBound().exponent());
        java.math.BigDecimal result1 = java.math.BigDecimal.valueOf(decoder.increment().mantissa(), -decoder.increment().exponent());
        return new Tiers(result0, result1);
    }
}
}

