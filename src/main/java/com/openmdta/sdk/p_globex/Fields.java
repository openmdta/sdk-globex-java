package com.openmdta.sdk.p_globex;

 public final class Fields {private Fields() {} public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder> GLOBEX__DISPLAY_NAME = new CatalogField<>("globex::DisplayName", new Format(0, 19311, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_0.DisplayNameDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_1.LusDomainObjectDecoder> LUS__LUS_DOMAIN_OBJECT = new CatalogField<>("lus::LusDomainObject", new Format(65001, 65011, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_1.LusDomainObjectDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_1.LusNameHashDecoder> LUS__LUS_NAME_HASH = new CatalogField<>("lus::LusNameHash", new Format(65001, 65012, 0, 0), com.openmdta.sdk.p_globex.sbe.catalog_1.LusNameHashDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskDecoder> OPENMDTA__BID_ASK = new CatalogField<>("openmdta::BidAsk", new Format(100, 10, 4, 27), com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder> OPENMDTA__TRADE = new CatalogField<>("openmdta::Trade", new Format(100, 11, 4, 18), com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleDecoder> OPENMDTA__BID_ASK_CANDLE = new CatalogField<>("openmdta::BidAskCandle", new Format(100, 12, 4, 89), com.openmdta.sdk.p_globex.sbe.catalog_2.BidAskCandleDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleDecoder> OPENMDTA__TRADE_CANDLE = new CatalogField<>("openmdta::TradeCandle", new Format(100, 13, 4, 52), com.openmdta.sdk.p_globex.sbe.catalog_2.TradeCandleDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleDecoder> OPENMDTA__TRADE_OHLCVV_CANDLE = new CatalogField<>("openmdta::TradeOhlcvvCandle", new Format(100, 21, 4, 61), com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvCandleDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.BidDailyOhlcDecoder> OPENMDTA__BID_DAILY_OHLC = new CatalogField<>("openmdta::BidDailyOhlc", new Format(100, 14, 4, 53), com.openmdta.sdk.p_globex.sbe.catalog_2.BidDailyOhlcDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.AskDailyOhlcDecoder> OPENMDTA__ASK_DAILY_OHLC = new CatalogField<>("openmdta::AskDailyOhlc", new Format(100, 15, 4, 53), com.openmdta.sdk.p_globex.sbe.catalog_2.AskDailyOhlcDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDailyOhlcDecoder> OPENMDTA__TRADE_DAILY_OHLC = new CatalogField<>("openmdta::TradeDailyOhlc", new Format(100, 16, 4, 53), com.openmdta.sdk.p_globex.sbe.catalog_2.TradeDailyOhlcDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.BidOhlcDecoder> OPENMDTA__BID_OHLC = new CatalogField<>("openmdta::BidOhlc", new Format(100, 17, 4, 52), com.openmdta.sdk.p_globex.sbe.catalog_2.BidOhlcDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.AskOhlcDecoder> OPENMDTA__ASK_OHLC = new CatalogField<>("openmdta::AskOhlc", new Format(100, 18, 4, 52), com.openmdta.sdk.p_globex.sbe.catalog_2.AskOhlcDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvDecoder> OPENMDTA__TRADE_OHLCVV = new CatalogField<>("openmdta::TradeOhlcvv", new Format(100, 19, 4, 61), com.openmdta.sdk.p_globex.sbe.catalog_2.TradeOhlcvvDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentDefinitionDecoder> OPENMDTA__INSTRUMENT_DEFINITION = new CatalogField<>("openmdta::InstrumentDefinition", new Format(100, 20, 4, 1), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentDefinitionDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingDecoder> OPENMDTA__LISTING = new CatalogField<>("openmdta::Listing", new Format(100, 30, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyProfileDecoder> OPENMDTA__COMPANY_PROFILE = new CatalogField<>("openmdta::CompanyProfile", new Format(100, 31, 4, 1), com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyProfileDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyAddressDecoder> OPENMDTA__COMPANY_ADDRESS = new CatalogField<>("openmdta::CompanyAddress", new Format(100, 32, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.CompanyAddressDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentIssuerDecoder> OPENMDTA__INSTRUMENT_ISSUER = new CatalogField<>("openmdta::InstrumentIssuer", new Format(100, 33, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentIssuerDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.BasicMasterdataDecoder> OPENMDTA__BASIC_MASTERDATA = new CatalogField<>("openmdta::BasicMasterdata", new Format(100, 34, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.BasicMasterdataDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder> OPENMDTA__INSTRUMENT_NAMES = new CatalogField<>("openmdta::InstrumentNames", new Format(100, 35, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentNamesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.LegalEntityNamesDecoder> OPENMDTA__LEGAL_ENTITY_NAMES = new CatalogField<>("openmdta::LegalEntityNames", new Format(100, 36, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.LegalEntityNamesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder> OPENMDTA__CLASSIFICATION = new CatalogField<>("openmdta::Classification", new Format(100, 37, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ClassificationDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder> OPENMDTA__LISTING_CLASSIFICATION = new CatalogField<>("openmdta::ListingClassification", new Format(100, 38, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingClassificationDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder> OPENMDTA__LISTING_QUOTATION = new CatalogField<>("openmdta::ListingQuotation", new Format(100, 39, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuotationDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder> OPENMDTA__LISTING_VENUE = new CatalogField<>("openmdta::ListingVenue", new Format(100, 40, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingVenueDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder> OPENMDTA__LISTING_TRADING_DATES = new CatalogField<>("openmdta::ListingTradingDates", new Format(100, 41, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingDatesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder> OPENMDTA__LISTING_TRADING_RULES = new CatalogField<>("openmdta::ListingTradingRules", new Format(100, 42, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTradingRulesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder> OPENMDTA__LISTING_QUOTE_PARAMETERS = new CatalogField<>("openmdta::ListingQuoteParameters", new Format(100, 43, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingQuoteParametersDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder> OPENMDTA__LISTING_ORDER_SIZE = new CatalogField<>("openmdta::ListingOrderSize", new Format(100, 44, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingOrderSizeDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder> OPENMDTA__LISTING_TICK_SCHEDULE = new CatalogField<>("openmdta::ListingTickSchedule", new Format(100, 45, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListingTickScheduleDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentPrimaryListingDecoder> OPENMDTA__INSTRUMENT_PRIMARY_LISTING = new CatalogField<>("openmdta::InstrumentPrimaryListing", new Format(100, 46, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentPrimaryListingDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentListingRecordCountDecoder> OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT = new CatalogField<>("openmdta::InstrumentListingRecordCount", new Format(100, 47, 4, 8), com.openmdta.sdk.p_globex.sbe.catalog_2.InstrumentListingRecordCountDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateActionsDecoder> OPENMDTA__CORPORATE_ACTIONS = new CatalogField<>("openmdta::CorporateActions", new Format(100, 48, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.CorporateActionsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder> OPENMDTA__DISTRIBUTIONS = new CatalogField<>("openmdta::Distributions", new Format(100, 49, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.DistributionsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_2.ListDefinitionDecoder> OPENMDTA__LIST_DEFINITION = new CatalogField<>("openmdta::ListDefinition", new Format(100, 50, 4, 0), com.openmdta.sdk.p_globex.sbe.catalog_2.ListDefinitionDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraRecordDecoder> XETRA__XETRA_RECORD = new CatalogField<>("xetra::XetraRecord", new Format(201, 1, 1, 3), com.openmdta.sdk.p_globex.sbe.catalog_3.XetraRecordDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.IdentifiersDecoder> XETRA__IDENTIFIERS = new CatalogField<>("xetra::Identifiers", new Format(201, 2, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.IdentifiersDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.DisplayNamesDecoder> XETRA__DISPLAY_NAMES = new CatalogField<>("xetra::DisplayNames", new Format(201, 3, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.DisplayNamesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder> XETRA__CLASSIFICATION = new CatalogField<>("xetra::Classification", new Format(201, 4, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.ClassificationDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.LifecycleDecoder> XETRA__LIFECYCLE = new CatalogField<>("xetra::Lifecycle", new Format(201, 5, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.LifecycleDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingVenueDecoder> XETRA__TRADING_VENUE = new CatalogField<>("xetra::TradingVenue", new Format(201, 6, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.TradingVenueDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingDatesDecoder> XETRA__TRADING_DATES = new CatalogField<>("xetra::TradingDates", new Format(201, 7, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.TradingDatesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.QuotationDecoder> XETRA__QUOTATION = new CatalogField<>("xetra::Quotation", new Format(201, 8, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.QuotationDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.TradingRulesDecoder> XETRA__TRADING_RULES = new CatalogField<>("xetra::TradingRules", new Format(201, 9, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.TradingRulesDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.MidpointTradingDecoder> XETRA__MIDPOINT_TRADING = new CatalogField<>("xetra::MidpointTrading", new Format(201, 10, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.MidpointTradingDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.SettlementDecoder> XETRA__SETTLEMENT = new CatalogField<>("xetra::Settlement", new Format(201, 11, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.SettlementDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteParametersDecoder> XETRA__QUOTE_PARAMETERS = new CatalogField<>("xetra::QuoteParameters", new Format(201, 12, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.QuoteParametersDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderSizeDecoder> XETRA__ORDER_SIZE = new CatalogField<>("xetra::OrderSize", new Format(201, 13, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.OrderSizeDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.OrderRiskLimitsDecoder> XETRA__ORDER_RISK_LIMITS = new CatalogField<>("xetra::OrderRiskLimits", new Format(201, 14, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.OrderRiskLimitsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.IcebergRequirementsDecoder> XETRA__ICEBERG_REQUIREMENTS = new CatalogField<>("xetra::IcebergRequirements", new Format(201, 15, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.IcebergRequirementsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.TickSizeTableDecoder> XETRA__TICK_SIZE_TABLE = new CatalogField<>("xetra::TickSizeTable", new Format(201, 16, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.TickSizeTableDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.DesignatedSponsorsDecoder> XETRA__DESIGNATED_SPONSORS = new CatalogField<>("xetra::DesignatedSponsors", new Format(201, 17, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.DesignatedSponsorsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.MarketMakersDecoder> XETRA__MARKET_MAKERS = new CatalogField<>("xetra::MarketMakers", new Format(201, 18, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.MarketMakersDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.SpecialistsDecoder> XETRA__SPECIALISTS = new CatalogField<>("xetra::Specialists", new Format(201, 19, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.SpecialistsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.RegulatoryLiquidityDecoder> XETRA__REGULATORY_LIQUIDITY = new CatalogField<>("xetra::RegulatoryLiquidity", new Format(201, 20, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.RegulatoryLiquidityDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityCorridorsDecoder> XETRA__VOLATILITY_CORRIDORS = new CatalogField<>("xetra::VolatilityCorridors", new Format(201, 21, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityCorridorsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityInterruptionLimitsDecoder> XETRA__VOLATILITY_INTERRUPTION_LIMITS = new CatalogField<>("xetra::VolatilityInterruptionLimits", new Format(201, 22, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.VolatilityInterruptionLimitsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.SecurityTermsDecoder> XETRA__SECURITY_TERMS = new CatalogField<>("xetra::SecurityTerms", new Format(201, 23, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.SecurityTermsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.DerivativeTermsDecoder> XETRA__DERIVATIVE_TERMS = new CatalogField<>("xetra::DerivativeTerms", new Format(201, 24, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.DerivativeTermsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.CouponTermsDecoder> XETRA__COUPON_TERMS = new CatalogField<>("xetra::CouponTerms", new Format(201, 25, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.CouponTermsDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionDecoder> XETRA__CORPORATE_ACTION = new CatalogField<>("xetra::CorporateAction", new Format(201, 26, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.CorporateActionDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.FeedPartitionDecoder> XETRA__FEED_PARTITION = new CatalogField<>("xetra::FeedPartition", new Format(201, 27, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.FeedPartitionDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.EmdiDecoder> XETRA__EMDI = new CatalogField<>("xetra::Emdi", new Format(201, 28, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.EmdiDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.MdiDecoder> XETRA__MDI = new CatalogField<>("xetra::Mdi", new Format(201, 29, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.MdiDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.EobiDecoder> XETRA__EOBI = new CatalogField<>("xetra::Eobi", new Format(201, 30, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.EobiDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentInputDecoder> XETRA__INSTRUMENT_INPUT = new CatalogField<>("xetra::InstrumentInput", new Format(201, 100, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.InstrumentInputDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraListingDecoder> XETRA__XETRA_LISTING = new CatalogField<>("xetra::XetraListing", new Format(201, 101, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.XetraListingDecoder::new);

public static final CatalogField<com.openmdta.sdk.p_globex.sbe.catalog_3.XetraMarketDetailsDecoder> XETRA__XETRA_MARKET_DETAILS = new CatalogField<>("xetra::XetraMarketDetails", new Format(201, 102, 1, 0), com.openmdta.sdk.p_globex.sbe.catalog_3.XetraMarketDetailsDecoder::new);

public static final java.util.List<CatalogField<?>> ALL = java.util.List.of(GLOBEX__DISPLAY_NAME, LUS__LUS_DOMAIN_OBJECT, LUS__LUS_NAME_HASH, OPENMDTA__BID_ASK, OPENMDTA__TRADE, OPENMDTA__BID_ASK_CANDLE, OPENMDTA__TRADE_CANDLE, OPENMDTA__TRADE_OHLCVV_CANDLE, OPENMDTA__BID_DAILY_OHLC, OPENMDTA__ASK_DAILY_OHLC, OPENMDTA__TRADE_DAILY_OHLC, OPENMDTA__BID_OHLC, OPENMDTA__ASK_OHLC, OPENMDTA__TRADE_OHLCVV, OPENMDTA__INSTRUMENT_DEFINITION, OPENMDTA__LISTING, OPENMDTA__COMPANY_PROFILE, OPENMDTA__COMPANY_ADDRESS, OPENMDTA__INSTRUMENT_ISSUER, OPENMDTA__BASIC_MASTERDATA, OPENMDTA__INSTRUMENT_NAMES, OPENMDTA__LEGAL_ENTITY_NAMES, OPENMDTA__CLASSIFICATION, OPENMDTA__LISTING_CLASSIFICATION, OPENMDTA__LISTING_QUOTATION, OPENMDTA__LISTING_VENUE, OPENMDTA__LISTING_TRADING_DATES, OPENMDTA__LISTING_TRADING_RULES, OPENMDTA__LISTING_QUOTE_PARAMETERS, OPENMDTA__LISTING_ORDER_SIZE, OPENMDTA__LISTING_TICK_SCHEDULE, OPENMDTA__INSTRUMENT_PRIMARY_LISTING, OPENMDTA__INSTRUMENT_LISTING_RECORD_COUNT, OPENMDTA__CORPORATE_ACTIONS, OPENMDTA__DISTRIBUTIONS, OPENMDTA__LIST_DEFINITION, XETRA__XETRA_RECORD, XETRA__IDENTIFIERS, XETRA__DISPLAY_NAMES, XETRA__CLASSIFICATION, XETRA__LIFECYCLE, XETRA__TRADING_VENUE, XETRA__TRADING_DATES, XETRA__QUOTATION, XETRA__TRADING_RULES, XETRA__MIDPOINT_TRADING, XETRA__SETTLEMENT, XETRA__QUOTE_PARAMETERS, XETRA__ORDER_SIZE, XETRA__ORDER_RISK_LIMITS, XETRA__ICEBERG_REQUIREMENTS, XETRA__TICK_SIZE_TABLE, XETRA__DESIGNATED_SPONSORS, XETRA__MARKET_MAKERS, XETRA__SPECIALISTS, XETRA__REGULATORY_LIQUIDITY, XETRA__VOLATILITY_CORRIDORS, XETRA__VOLATILITY_INTERRUPTION_LIMITS, XETRA__SECURITY_TERMS, XETRA__DERIVATIVE_TERMS, XETRA__COUPON_TERMS, XETRA__CORPORATE_ACTION, XETRA__FEED_PARTITION, XETRA__EMDI, XETRA__MDI, XETRA__EOBI, XETRA__INSTRUMENT_INPUT, XETRA__XETRA_LISTING, XETRA__XETRA_MARKET_DETAILS);}
