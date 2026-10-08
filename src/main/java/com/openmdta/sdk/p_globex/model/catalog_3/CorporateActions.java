package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record CorporateActions(java.util.List<Events> events) {
    public CorporateActions {
        events = java.util.List.copyOf(events);
    }
    public static CorporateActions decode(com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Events>();
        var value0Group = decoder.events();
        while (value0Group.hasNext()) {
            value0.add(Events.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Events> result0 = java.util.List.copyOf(value0);
        return new CorporateActions(result0);
    }
public record Events(com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionKind kind, com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateEventStatus status, java.util.OptionalLong newShares, java.util.OptionalLong oldShares, java.util.OptionalLong announcementDay, java.util.OptionalLong recordDay, String eventId) {
    public static Events decode(com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder.EventsDecoder decoder, int actingVersion) {
        com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionKind result0 = decoder.kind();
        com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateEventStatus result1 = decoder.status();
        java.util.OptionalLong result2 = (decoder.newShares() == com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder.EventsDecoder.newSharesNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.newShares());
        java.util.OptionalLong result3 = (decoder.oldShares() == com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder.EventsDecoder.oldSharesNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.oldShares());
        java.util.OptionalLong result4 = (decoder.announcementDay() == com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder.EventsDecoder.announcementDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.announcementDay());
        java.util.OptionalLong result5 = (decoder.recordDay() == com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionsDecoder.EventsDecoder.recordDayNullValue()) ? java.util.OptionalLong.empty() : java.util.OptionalLong.of(decoder.recordDay());
        String result6 = decoder.eventId();
        return new Events(result0, result1, result2, result3, result4, result5, result6);
    }
}
}

