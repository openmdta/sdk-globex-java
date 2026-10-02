package com.openmdta.sdk.p_globex;

public final class DatasetFields {
    private DatasetFields() {}
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder> GLOBEX_GLOBEX_DISPLAY_NAME = new CatalogField<>("DisplayName", "globex::DisplayName", new Format(49499, 19311, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder::new);
    public static final java.util.List<CatalogField<?>> GLOBEX_ALL = java.util.List.of(GLOBEX_GLOBEX_DISPLAY_NAME);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> LUS_INSTRUMENT_NAMES = new CatalogField<>("instrument_names", "openmdta::InstrumentNames", new Format(41957, 61998, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder::new);
    public static final java.util.List<CatalogField<?>> LUS_ALL = java.util.List.of(LUS_INSTRUMENT_NAMES);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> SIM_INSTRUMENT_NAMES = new CatalogField<>("instrument_names", "openmdta::InstrumentNames", new Format(41957, 61998, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder::new);
    public static final java.util.List<CatalogField<?>> SIM_ALL = java.util.List.of(SIM_INSTRUMENT_NAMES);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> XETRA_CLASSIFICATION = new CatalogField<>("classification", "openmdta::Classification", new Format(41957, 53557, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> XETRA_INSTRUMENT_NAMES = new CatalogField<>("instrument_names", "openmdta::InstrumentNames", new Format(41957, 61998, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder> XETRA_LISTING_CLASSIFICATION = new CatalogField<>("listing_classification", "openmdta::ListingClassification", new Format(41957, 39307, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder> XETRA_LISTING_ORDER_SIZE = new CatalogField<>("listing_order_size", "openmdta::ListingOrderSize", new Format(41957, 23952, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder> XETRA_LISTING_QUOTATION = new CatalogField<>("listing_quotation", "openmdta::ListingQuotation", new Format(41957, 57471, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder> XETRA_LISTING_QUOTE_PARAMETERS = new CatalogField<>("listing_quote_parameters", "openmdta::ListingQuoteParameters", new Format(41957, 3567, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder> XETRA_LISTING_TICK_SCHEDULE = new CatalogField<>("listing_tick_schedule", "openmdta::ListingTickSchedule", new Format(41957, 17315, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder> XETRA_LISTING_TRADING_DATES = new CatalogField<>("listing_trading_dates", "openmdta::ListingTradingDates", new Format(41957, 37622, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder> XETRA_LISTING_TRADING_RULES = new CatalogField<>("listing_trading_rules", "openmdta::ListingTradingRules", new Format(41957, 34570, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder::new);
    public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder> XETRA_LISTING_VENUE = new CatalogField<>("listing_venue", "openmdta::ListingVenue", new Format(41957, 30232, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder::new);
    public static final java.util.List<CatalogField<?>> XETRA_ALL = java.util.List.of(XETRA_CLASSIFICATION, XETRA_INSTRUMENT_NAMES, XETRA_LISTING_CLASSIFICATION, XETRA_LISTING_ORDER_SIZE, XETRA_LISTING_QUOTATION, XETRA_LISTING_QUOTE_PARAMETERS, XETRA_LISTING_TICK_SCHEDULE, XETRA_LISTING_TRADING_DATES, XETRA_LISTING_TRADING_RULES, XETRA_LISTING_VENUE);
    public static java.util.List<CatalogField<?>> forDataset(String dataset) {
        return switch (dataset) {
            case "globex/globex" -> GLOBEX_ALL;
            case "globex/lus" -> LUS_ALL;
            case "globex/sim" -> SIM_ALL;
            case "globex/xetra" -> XETRA_ALL;
            default -> java.util.List.of();
        };
    }
}
