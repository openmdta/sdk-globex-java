package com.openmdta.sdk.p_globex.model.stream_0;

/** Immutable owned value; decoding allocates. */
public record Distributions(java.util.List<Events> events) {
    public Distributions {
        events = java.util.List.copyOf(events);
    }
    public static Distributions decode(com.openmdta.sdk.p_globex.sbe.stream_0.DistributionsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Events>();
        var value0Group = decoder.events();
        while (value0Group.hasNext()) {
            value0.add(Events.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Events> result0 = java.util.List.copyOf(value0);
        return new Distributions(result0);
    }
public record Events(com.openmdta.sdk.p_globex.sbe.stream_0.DistributionKind kind, com.openmdta.sdk.p_globex.sbe.stream_0.CorporateEventStatus status, java.math.BigDecimal amount, java.util.OptionalLong recordDay, java.util.OptionalLong paymentDay, java.util.List<Byte> currency, java.util.List<Byte> eventId) {
    public Events {
        currency = java.util.List.copyOf(currency);
        eventId = java.util.List.copyOf(eventId);
    }
    public static Events decode(com.openmdta.sdk.p_globex.sbe.stream_0.DistributionsDecoder.EventsDecoder decoder, int actingVersion) {
        com.openmdta.sdk.p_globex.sbe.stream_0.DistributionKind result0 = decoder.kind();
        com.openmdta.sdk.p_globex.sbe.stream_0.CorporateEventStatus result1 = decoder.status();
        java.math.BigDecimal result2 = java.math.BigDecimal.valueOf(decoder.amount().mantissa(), -decoder.amount().exponent());
        java.util.OptionalLong result3 = (decoder.recordDay() == com.openmdta.sdk.p_globex.sbe.stream_0.DistributionsDecoder.EventsDecoder.recordDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.recordDay());
        java.util.OptionalLong result4 = (decoder.paymentDay() == com.openmdta.sdk.p_globex.sbe.stream_0.DistributionsDecoder.EventsDecoder.paymentDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.paymentDay());
        byte[] value5Bytes = new byte[decoder.currencyLength()];
        decoder.getCurrency(value5Bytes, 0, value5Bytes.length);
        var value5 = new java.util.ArrayList<Byte>(value5Bytes.length);
        for (byte item : value5Bytes) { value5.add(item); }
        java.util.List<Byte> result5 = java.util.List.copyOf(value5);
        byte[] value6Bytes = new byte[decoder.eventIdLength()];
        decoder.getEventId(value6Bytes, 0, value6Bytes.length);
        var value6 = new java.util.ArrayList<Byte>(value6Bytes.length);
        for (byte item : value6Bytes) { value6.add(item); }
        java.util.List<Byte> result6 = java.util.List.copyOf(value6);
        return new Events(result0, result1, result2, result3, result4, result5, result6);
    }
}
}

