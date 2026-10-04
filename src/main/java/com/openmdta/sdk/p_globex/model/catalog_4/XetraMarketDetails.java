package com.openmdta.sdk.p_globex.model.catalog_4;

/** Immutable owned value; decoding allocates. */
public record XetraMarketDetails(java.util.List<Participants> participants, String liquidityProviderUserGroup, String specialistUserGroup) {
    public XetraMarketDetails {
        participants = java.util.List.copyOf(participants);
    }
    public static XetraMarketDetails decode(com.openmdta.sdk.p_globex.sbe.catalog_4.XetraMarketDetailsDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        var value0 = new java.util.ArrayList<Participants>();
        var value0Group = decoder.participants();
        while (value0Group.hasNext()) {
            value0.add(Participants.decode(value0Group.next(), actingVersion));
        }
        java.util.List<Participants> result0 = java.util.List.copyOf(value0);
        String result1 = decoder.liquidityProviderUserGroup();
        String result2 = decoder.specialistUserGroup();
        return new XetraMarketDetails(result0, result1, result2);
    }
public record Participants(String role, String memberId, String name) {
    public static Participants decode(com.openmdta.sdk.p_globex.sbe.catalog_4.XetraMarketDetailsDecoder.ParticipantsDecoder decoder, int actingVersion) {
        String result0 = decoder.role();
        String result1 = decoder.memberId();
        String result2 = decoder.name();
        return new Participants(result0, result1, result2);
    }
}
}

