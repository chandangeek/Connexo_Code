/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.impl.LicenseServiceImpl;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerFormulaBuilder;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method
 * when temperature conversion (K, °C, °F) is required.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (10:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateWithTemperatureConversionIT {

    private static final String DAILY_TEMPERATURE_CELCIUS_MRID = "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.23.0";
    private static final String DAILY_TEMPERATURE_FAHRENHEIT_MRID = "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.279.0";
    private static final String DAILY_TEMPERATURE_KELVIN_MRID = "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.6.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType K_15min;
    private static ReadingType C_15min;
    private static ReadingType F_15min;
    private static ReadingType K_daily;
    private static ReadingType C_daily;
    private static ReadingType F_daily;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static MeterRole DEFAULT_METER_ROLE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static MeteringDataModelService dataModelService;
    private static Thesaurus thesaurus;
    private long temperature1RequirementId;
    private long temperature2RequirementId;
    private long deliverableId;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private MetrologyContract contract;
    private SqlBuilder temperatureWithClauseBuilder;
    private SqlBuilder deliverableWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;
    private MeterActivation meterActivation;
    private Channel temperatureChannel;
    private UsagePoint usagePoint;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
            bind(SearchService.class).toInstance(mockSearchService());
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        dataModelService = mock(MeteringDataModelService.class);
        setupThesaurus();
        setupServices();
        setupReadingTypes();
        setupMetrologyPurposeAndRole();
        setupDefaultUsagePointLifeCycle();
    }

    private static void setupThesaurus() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        thesaurus = mock(Thesaurus.class);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        when(dataModelService.getThesaurus()).thenReturn(thesaurus);
    }

    private static void setupServices() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(
                            "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.6.0",    // macro period: daily, averages, Kelvin
                            "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.23.0",   // macro period: daily, averages, degrees celcius
                            "11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.279.0",  // macro period: daily, averages, degrees Fahrenheit
                            "0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.6.0",     // no macro period, 15 min, Kelvin
                            "0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.23.0",    // no macro period, 15 min, degrees Celcius
                            "0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.279.0"    // no macro period, 15 min, degrees Fahrenheit
                    ),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new UserModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule(),
                    new TaskModule(),
                    new CalendarModule(),
                    new CustomPropertySetsModule(),
                    new UsagePointLifeCycleConfigurationModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getMeteringService();
            getDataAggregationService();
            ctx.commit();
        }
    }

    private static MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private static SqlBuilderFactory getSqlBuilderFactory() {
        return sqlBuilderFactory;
    }

    private static DataAggregationService getDataAggregationService() {
        ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
        return new DataAggregationServiceImpl(
                injector.getInstance(CalendarService.class),
                injector.getInstance(CustomPropertySetService.class),
                meteringService,
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplCalculateWithTemperatureConversionIT::getSqlBuilderFactory,
                () -> new VirtualFactoryImpl(dataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            K_15min = getMeteringService().getReadingType("0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.6.0").get();
            C_15min = getMeteringService().getReadingType("0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.23.0").get();
            F_15min = getMeteringService().getReadingType("0.0.2.0.0.4.46.0.0.0.0.0.0.0.0.0.279.0").get();
            K_daily = getMeteringService().getReadingType("11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.6.0").get();
            C_daily = getMeteringService().getReadingType("11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.23.0").get();
            F_daily = getMeteringService().getReadingType("11.2.0.0.0.4.46.0.0.0.0.0.0.0.0.0.279.0").get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurposeAndRole() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplCalculateWithTemperatureConversionIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplCalculateWithTemperatureConversionIT.class.getSimpleName() + ".description");
            when(description.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(description.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(description.getLayer()).thenReturn(Layer.DOMAIN);
            METROLOGY_PURPOSE = getMetrologyConfigurationService().createMetrologyPurpose(name, description);
            DEFAULT_METER_ROLE = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            ELECTRICITY = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
            ELECTRICITY.addMeterRole(DEFAULT_METER_ROLE);
            ctx.commit();
        }
    }

    private static void setupDefaultUsagePointLifeCycle() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
            usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        this.temperatureWithClauseBuilder = new SqlBuilder();
        this.deliverableWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + temperature1RequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.temperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + deliverableId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.deliverableWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
    }

    private static SearchService mockSearchService() {
        SearchService searchService = mock(SearchService.class);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(any())).thenReturn(Optional.of(searchDomain));
        return searchService;
    }


    @After
    public void resetSqlBuilder() {
        reset(sqlBuilderFactory);
        reset(clauseAwareSqlBuilder);
    }

    /**
     * Tests the unit conversion K -> °C
     * Metrology configuration
     *    requirements:
     *       T ::= any temperature (15m)
     *    deliverables:
     *       averageTemperature (daily °C) ::= T + 10
     * Device:
     *    meter activations:
     *       Jan 1st 2016 -> forever
     *           T -> 15 min K
     * In other words, the requirement is provided by exactly
     * one matching channel from a single meter activation
     * but the temparature channel needs to be converted from
     * Kelvin to degrees Celcius while aggregating.
     */
    @Test
    @Transactional
    public void kelvinToCelcius() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("kelvinToCelcius");
        this.setupUsagePoint("kelvinToCelcius");
        this.activateMeterWithKelvin();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("kelvinToCelcius", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement temperature = this.configuration.newReadingTypeRequirement("T", DEFAULT_METER_ROLE)
                .withReadingType(K_15min);
        this.temperature1RequirementId = temperature.getId();

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder =
                newDeliveryBuilder("averageT", configuration, C_daily);
        ReadingTypeDeliverable avgTemperature =
                builder.build(builder.plus(
                        builder.requirement(temperature),
                        builder.constant(BigDecimal.TEN)));

        this.deliverableId = avgTemperature.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(avgTemperature);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperature1RequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.timestamp,.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.value\\s*\\+\\s*\\?\\s*\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_CELCIUS_MRID) + "'.*");
            /* Assert that the overall select statement converts the Kelvin values to Celcius
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(rod" + deliverableId + "_1\\.value\\s*-\\s*273\\.15\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
        }
    }

    /**
     * Tests the unit conversion K -> °C
     * Metrology configuration
     *    requirements:
     *       T ::= any temperature (15m)
     *    deliverables:
     *       averageTemperature (daily °F) ::= T + 10
     * Device:
     *    meter activations:
     *       Jan 1st 2016 -> forever
     *           T -> 15 min K
     * In other words, the requirement is provided by exactly
     * one matching channel from a single meter activation
     * but the temparature channel needs to be converted from
     * Kelvin to degrees Fahrenheit while aggregating.
     */
    @Test
    @Transactional
    public void kelvinToFahrenheit() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("kelvinToFahrenheit");
        this.setupUsagePoint("kelvinToFahrenheit");
        this.activateMeterWithKelvin();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("kelvingToFahrenheit", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement temperature = this.configuration.newReadingTypeRequirement("T", DEFAULT_METER_ROLE)
                .withReadingType(K_15min);
        this.temperature1RequirementId = temperature.getId();

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder =
                newDeliveryBuilder("averageT", configuration, F_daily);
        ReadingTypeDeliverable avgTemperature =
                builder.build(builder.plus(
                        builder.requirement(temperature),
                        builder.constant(BigDecimal.TEN)));


        this.deliverableId = avgTemperature.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(avgTemperature);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperature1RequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.timestamp,.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.value\\s*\\+\\s*\\?\\s*\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_FAHRENHEIT_MRID) + "'.*");
            /* Assert that the overall select statement converts the Kelvin values to Celcius
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(\\(9\\s*\\*\\s*\\(rod" + deliverableId + "_1\\.value\\s*-\\s*255\\.3722*\\)\\)\\s*/\\s*5\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
        }
    }

    /**
     * Tests the unit conversion to K
     * Metrology configuration
     *    requirements:
     *       minT ::= any temperature (15m)
     *       maxT ::= any temperature (15m)
     *    deliverables:
     *       averageTemperature (daily K) ::= (minT + maxT) / 2
     * Device:
     *    meter activations:
     *       Jan 1st 2016 -> forever
     *           minT -> 15 min °C
     *           maxT -> 15 min °F
     * In other words, the requirement is provided by exactly
     * one matching channel from a single meter activation
     * but the temparature channel needs to be converted from
     * Kelvin to degrees Celcius while aggregating.
     */
    @Test
    @Transactional
    public void celciusAndFahrenheitToKelvin() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("celciusAndFahrenheitToKelvin");
        this.setupUsagePoint("celciusAndFahrenheitToKelvin");
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        Channel minTChannel = this.meterActivation.getChannelsContainer().createChannel(C_15min);
        Channel maxTChannel = this.meterActivation.getChannelsContainer().createChannel(F_15min);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("celciusAndFahrenheitToKelvin", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement minTemperature = this.configuration.newReadingTypeRequirement("minT", DEFAULT_METER_ROLE)
                .withReadingType(C_15min);
        this.temperature1RequirementId = minTemperature.getId();
        ReadingTypeRequirement maxTemperature = this.configuration.newReadingTypeRequirement("maxT", DEFAULT_METER_ROLE)
                .withReadingType(F_15min);
        this.temperature2RequirementId = maxTemperature.getId();

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = newDeliveryBuilder("averageT", configuration, K_daily);
        ReadingTypeDeliverable avgTemperature =
                builder.build(builder.divide(
                        builder.plus(
                                builder.requirement(minTemperature),
                                builder.requirement(maxTemperature)),
                        builder.constant(BigDecimal.valueOf(2L))));

        this.deliverableId = avgTemperature.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        SqlBuilder minTemperatureWithClauseBuilder = new SqlBuilder();
        SqlBuilder maxTemperatureWithClauseBuilder = new SqlBuilder();
        when(clauseAwareSqlBuilder.with(matches("rid" + temperature1RequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(minTemperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + temperature2RequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(maxTemperatureWithClauseBuilder);

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(avgTemperature);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperature1RequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(minTemperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperature2RequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(maxTemperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.timestamp,.*");
            // Assert that the min temperature requirements' value is not converted
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid" + temperature1RequirementId + "_" + deliverableId + "_1\\.value\\s*\\+\\s*\\(.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the max temperature requirements' value is coverted to Celcius
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(255.3722*\\s*\\+\\s*\\(\\(5\\s*\\*\\s*rid" + temperature2RequirementId + "_" + deliverableId + "_1\\.value\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_KELVIN_MRID) + "'.*");
            /* Assert that the overall select statement converts the Celcius values to Kelvin
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(273.15*\\s*\\+\\s*rod" + deliverableId + "_1\\.value\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod" + deliverableId + "_1\\.localdate, 'DDD'\\).*");
        }
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

    private static ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(ServerMetrologyConfigurationService.class);
    }

    private static ServerFormulaBuilder newFormulaBuilder() {
        return getMetrologyConfigurationService().newFormulaBuilder(Formula.Mode.AUTO);
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase, amrIdBase).create();
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
    }

    private void activateMeterWithKelvin() {
        this.activateMeter(K_15min);
    }

    private void activateMeter(ReadingType readingType) {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        this.temperatureChannel = this.meterActivation.getChannelsContainer().createChannel(readingType);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

    private ReadingTypeDeliverableBuilder newDeliveryBuilder(String name, MetrologyConfiguration configuration, ReadingType readingType) {
        return configuration.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);

    }

}
