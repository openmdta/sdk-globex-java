package com.openmdta.sdk.p_globex.model.catalog_2;

/** Immutable owned value; decoding allocates. */
public record Distributions(java.util.List<Events> events) {
    public Distributions {
        events = java.util.List.copyOf(events);
    }
    public static Distributions decode(com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Events>();
        var value0Group = decoder.events();
        while (value0Group.hasNext()) {
            value0.add(Events.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Events> result0 = java.util.List.copyOf(value0);
        return new Distributions(result0);
    }
public record Events(com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionKind kind, com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateEventStatus status, java.math.BigDecimal amount, java.util.OptionalLong recordDay, java.util.OptionalLong paymentDay, String currency, String eventId) {
    public static Events decode(com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder.EventsDecoder decoder, int actingVersion) {
        com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionKind result0 = decoder.kind();
        com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateEventStatus result1 = decoder.status();
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.amount().mantissa(), -decoder.amount().exponent());
        java.util.OptionalLong result3 = (decoder.recordDay() == com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder.EventsDecoder.recordDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.recordDay());
        java.util.OptionalLong result4 = (decoder.paymentDay() == com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder.EventsDecoder.paymentDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.paymentDay());
        String result5 = decoder.currency();
        String result6 = decoder.eventId();
        return new Events(result0, result1, result2, result3, result4, result5, result6);
    }
}
}

