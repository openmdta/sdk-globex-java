package com.openmdta.sdk.p_globex.model.catalog_3;

/** Immutable owned value; decoding allocates. */
public record CorporateAction(String cum_ex_indicator) {
    public static CorporateAction decode(com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionDecoder decoder) {
        int actingVersion = decoder.actingVersion();
        String result0 = decoder.cum_ex_indicator();
        return new CorporateAction(result0);
    }
}

