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
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
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
 * for {@link Formula}s in {@link com.elster.jupiter.metering.config.Formula.Mode#EXPERT expert mode}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-18 (10:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplExpertModeIT {

    private static final String CELCIUS_15_MIN_MRID = "0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0";
    private static final String MILLIBAR_15_MIN_MRID = "0.0.2.0.0.7.0.0.0.0.0.0.0.0.0.0.214.0";
    private static final String MEGA_JOULE_15_MIN_MRID = "0.0.2.0.0.7.12.0.0.0.0.0.0.0.0.6.31.0";
    private static final String MEGA_JOULE_DAILY_MRID = "11.2.2.0.0.7.12.0.0.0.0.0.0.0.0.6.31.0";
    private static final String CUBIC_METER_15_MIN_MRID = "0.0.2.0.0.7.58.0.0.0.0.0.0.0.0.0.42.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType CELCIUS_15min;
    private static ReadingType PRESSURE_15min;
    private static ReadingType VOLUME_15min;
    private static ReadingType ENERGY_15min;
    private static ReadingType ENERGY_daily;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static MeterRole DEFAULT_METER_ROLE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static MeteringDataModelService dataModelService;
    private static Thesaurus thesaurus;
    private long temperatureRequirementId;
    private long pressureRequirementId;
    private long volumeRequirementId;
    private long deliverableId;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    private UsagePointMetrologyConfiguration configuration;
    private MetrologyPurpose metrologyPurpose;
    private MetrologyContract contract;
    private SqlBuilder temperatureWithClauseBuilder;
    private SqlBuilder pressureWithClauseBuilder;
    private SqlBuilder volumeWithClauseBuilder;
    private SqlBuilder deliverableWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;
    private MeterActivation meterActivation;
    private Channel temperatureChannel;
    private Channel pressureChannel;
    private Channel volumeChannel;
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
        when(thesaurus.getFormat(any((TranslationKey.class)))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any((MessageSeed.class)))).thenReturn(messageFormat);
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
                            MEGA_JOULE_15_MIN_MRID,
                            MEGA_JOULE_DAILY_MRID,
                            MILLIBAR_15_MIN_MRID,
                            CELCIUS_15_MIN_MRID,
                            CUBIC_METER_15_MIN_MRID
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
                    new CalendarModule(),
                    new CustomPropertySetsModule(),
                    new BasicPropertiesModule(),
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
                mock(CalendarService.class),
                mock(CustomPropertySetService.class),
                meteringService,
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplExpertModeIT::getSqlBuilderFactory,
                () -> new VirtualFactoryImpl(dataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            CELCIUS_15min = getMeteringService().getReadingType(CELCIUS_15_MIN_MRID).get();
            PRESSURE_15min = getMeteringService().getReadingType(MILLIBAR_15_MIN_MRID).get();
            VOLUME_15min = getMeteringService().getReadingType(CUBIC_METER_15_MIN_MRID).get();
            ENERGY_15min = getMeteringService().getReadingType(MEGA_JOULE_15_MIN_MRID).get();
            ENERGY_daily = getMeteringService().getReadingType(MEGA_JOULE_DAILY_MRID).get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurposeAndRole() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplExpertModeIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplExpertModeIT.class.getSimpleName() + ".description");
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
        this.pressureWithClauseBuilder = new SqlBuilder();
        this.volumeWithClauseBuilder = new SqlBuilder();
        this.deliverableWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + temperatureRequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.temperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + pressureRequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.pressureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + volumeRequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.volumeWithClauseBuilder);
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
     * Tests the expert mode formula: Energy = Normalized volume * calorific value
     * where Normalized volume is calculated as: volume * (temperature / pressure * normalized-temperature / normalized-pressure).
     * Both normalized-temperature and normalized-pressure are in fact constants.
     * Metrology configuration
     * requirements:
     * T ::= Celcius  (15m)
     * P ::= millibar (15m)
     * V ::= m3       (15m)
     * deliverables:
     * Energy (15min °C) ::= V * (T / P * 1013.25 / 288.15)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * T -> 15 min °C
     * P -> 15 min millibar
     * V -> 15 min m3
     * In other words, all requirements are provided by exactly
     * one matching channel from a single meter activation.
     */
    @Test
    @Transactional
    public void energyFromGasVolume() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("energyFromGasVolume");
        this.setupUsagePoint("energyFromGasVolume");
        this.activateMeter();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("energyFromGasVolume", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement temperature = this.configuration.newReadingTypeRequirement("T", DEFAULT_METER_ROLE)
                .withReadingType(CELCIUS_15min);
        this.temperatureRequirementId = temperature.getId();
        ReadingTypeRequirement pressure = this.configuration.newReadingTypeRequirement("P", DEFAULT_METER_ROLE)
                .withReadingType(PRESSURE_15min);
        this.pressureRequirementId = pressure.getId();
        ReadingTypeRequirement volume = this.configuration.newReadingTypeRequirement("V", DEFAULT_METER_ROLE)
                .withReadingType(VOLUME_15min);
        this.volumeRequirementId = volume.getId();

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("Energy", ENERGY_15min, Formula.Mode.EXPERT);
        ReadingTypeDeliverable energy = builder.build(
                builder.multiply(
                        builder.constant(BigDecimal.valueOf(40L)),   // calorific value
                        builder.multiply(
                                builder.requirement(volume),
                                builder.multiply(
                                        builder.divide(
                                                builder.requirement(temperature),
                                                builder.requirement(pressure)),
                                        builder.divide(
                                                builder.constant(BigDecimal.valueOf(101325L, 2)),    // 1013,25 normal pressure at sea level
                                                builder.constant(BigDecimal.valueOf(15L)))))));
        this.deliverableId = energy.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(energy);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperatureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + pressureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(pressureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + volumeRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(volumeWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            String deliverableWithClauseSql = this.deliverableWithClauseBuilder.getText().replace("\n", " ");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT -1, rid" + volumeRequirementId + "_" + deliverableId + "_1\\.timestamp,.*rid" + volumeRequirementId + "_" + deliverableId + "_1\\.readingQuality,.*rid" + volumeRequirementId + "_" + deliverableId + "_1\\.localdate\\s*FROM.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*\\(\\s*\\?\\s*\\* \\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.value \\* \\(\\(rid" + temperatureRequirementId + "_" + deliverableId + "_1\\.value / rid" + pressureRequirementId + "_" + deliverableId + "_1\\.value\\) \\* \\(\\s*\\?\\s*/\\s*\\?\\s*\\)\\)\\)\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MEGA_JOULE_15_MIN_MRID) + "'.*");
            /* Assert that the overall select statement selects the value from the deliverable. */
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + deliverableId + "_1\\.value.*");
        }
    }

    /**
     * Tests the expert mode formula: Energy = Normalized volume * calorific value
     * where Normalized volume is calculated as: volume * (temperature / pressure * normalized-temperature / normalized-pressure).
     * Both normalized-temperature and normalized-pressure are in fact constants.
     * Metrology configuration
     * requirements:
     * T ::= Celcius  (15m)
     * P ::= millibar (15m)
     * V ::= m3       (15m)
     * deliverables:
     * Energy (daily °C) ::= V * (T / P * 1013.25 / 288.15)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * T -> 15 min °C
     * P -> 15 min millibar
     * V -> 15 min m3
     * In other words, all requirements are provided by exactly
     * one matching channel from a single meter activation.
     */
    @Test
    @Transactional
    public void energyFromGasVolumeWithDailyAggregation() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("energyFromGasVolume");
        this.setupUsagePoint("energyFromGasVolume");
        this.activateMeter();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("energyFromGasVolume", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement temperature = this.configuration.newReadingTypeRequirement("T", DEFAULT_METER_ROLE)
                .withReadingType(CELCIUS_15min);
        this.temperatureRequirementId = temperature.getId();
        ReadingTypeRequirement pressure = this.configuration.newReadingTypeRequirement("P", DEFAULT_METER_ROLE)
                .withReadingType(PRESSURE_15min);
        this.pressureRequirementId = pressure.getId();
        ReadingTypeRequirement volume = this.configuration.newReadingTypeRequirement("V", DEFAULT_METER_ROLE)
                .withReadingType(VOLUME_15min);
        this.volumeRequirementId = volume.getId();

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable(
                "Energy",
                ENERGY_daily,
                Formula.Mode.EXPERT);
        ReadingTypeDeliverable energy =
                builder.build(
                        builder.aggregate(   // Note how the expert is required to define when the aggregation is done
                                builder.multiply(
                                        builder.constant(BigDecimal.valueOf(40L)),   // calorific value
                                        builder.multiply(
                                                builder.requirement(volume),
                                                builder.multiply(
                                                        builder.divide(
                                                                builder.requirement(temperature),
                                                                builder.requirement(pressure)),
                                                        builder.divide(
                                                                builder.constant(BigDecimal.valueOf(101325L, 2)),    // 1013,25 normal pressure at sea level
                                                                builder.constant(BigDecimal.valueOf(15L))))))));     // 15 normalized gas is measured at 15 °Celcius
        this.deliverableId = energy.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(energy);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperatureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + pressureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(pressureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + volumeRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(volumeWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            String deliverableWithClauseSql = this.deliverableWithClauseBuilder.getText().replace("\n", " ");
            assertThat(deliverableWithClauseSql).startsWith("SELECT -1,");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[max|MAX]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.timestamp\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*MAX\\(.*rid" + volumeRequirementId + "_" + deliverableId + "_1\\.readingQuality.*\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[trunc|TRUNC]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.localdate, 'DDD'\\)\\s*FROM.*");
            // Assert that the formula and the aggregation function is applied to the requirements' value in the select clause
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[sum|SUM]\\(\\(\\s*\\?\\s*\\*\\s*\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.value \\* \\(\\(rid" + temperatureRequirementId + "_" + deliverableId + "_1\\.value / rid" + pressureRequirementId + "_" + deliverableId + "_1\\.value\\) \\* \\(\\s*\\?\\s*/\\s*\\?\\s*\\)\\)\\)\\)\\).*FROM.*");
            assertThat(deliverableWithClauseSql).matches(".*[group by trunc|GROUP BY TRUNC]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.localdate.*, 'DDD'\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MEGA_JOULE_DAILY_MRID) + "'.*");
            /* Assert that the overall select statement selects the value from the deliverable. */
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + deliverableId + "_1\\.value.*");
        }
    }

    /**
     * Tests the expert mode formula: Energy = Normalized volume * calorific value
     * where Normalized volume is calculated as: volume * (temperature / pressure * normalized-temperature / normalized-pressure).
     * Both normalized-temperature and normalized-pressure are in fact constants.
     * Metrology configuration
     * requirements:
     * T ::= Celcius  (15m)
     * P ::= millibar (daily)
     * V ::= m3       (15m)
     * deliverables:
     * Energy (daily °C) ::= sum(V, day) * (avg(T, day) / P * 1013.25 / 288.15)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * T -> 15 min °C
     * P -> 15 min millibar
     * V -> 15 min m3
     * In other words, all requirements are provided by exactly
     * one matching channel from a single meter activation.
     */
    @Test
    @Transactional
    public void energyFromGasVolumeWithManualDailyAggregation() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("energyFromGasVolume");
        this.setupUsagePoint("energyFromGasVolume");
        this.activateMeter();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("energyFromGasVolume", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        ReadingTypeRequirement temperature = this.configuration.newReadingTypeRequirement("T", DEFAULT_METER_ROLE)
                .withReadingType(CELCIUS_15min);
        this.temperatureRequirementId = temperature.getId();
        System.out.println("temperatureRequirementId = " + this.temperatureRequirementId);
        ReadingTypeRequirement pressure = this.configuration.newReadingTypeRequirement("P", DEFAULT_METER_ROLE)
                .withReadingType(PRESSURE_15min);
        this.pressureRequirementId = pressure.getId();
        System.out.println("pressureRequirementId = " + this.pressureRequirementId);
        ReadingTypeRequirement volume = this.configuration.newReadingTypeRequirement("V", DEFAULT_METER_ROLE)
                .withReadingType(VOLUME_15min);
        this.volumeRequirementId = volume.getId();
        System.out.println("volumeRequirementId = " + this.volumeRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable(
                "Energy",
                ENERGY_daily,
                Formula.Mode.EXPERT);
        ReadingTypeDeliverable energy =
                builder.build(
                        builder.multiply(
                                builder.constant(BigDecimal.valueOf(40L)),   // calorific value
                                builder.multiply(
                                        builder.sum(AggregationLevel.DAY, builder.requirement(volume)),
                                        builder.multiply(
                                                builder.divide(
                                                        builder.average(AggregationLevel.DAY, builder.requirement(temperature)),
                                                        builder.requirement(pressure)),
                                                builder.divide(
                                                        builder.constant(BigDecimal.valueOf(101325L, 2)),    // 1013,25 normal pressure at sea level
                                                        builder.constant(BigDecimal.valueOf(15L)))))));     // 15 normalized gas is measured at 15 °Celcius
        this.deliverableId = energy.getId();

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(energy);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + temperatureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + pressureRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(pressureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + volumeRequirementId + ".*" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(volumeWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + deliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            String deliverableWithClauseSql = this.deliverableWithClauseBuilder.getText().replace("\n", " ");
            assertThat(deliverableWithClauseSql).startsWith("SELECT -1,");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[max|MAX]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.timestamp\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*MAX\\(.*rid" + volumeRequirementId + "_" + deliverableId + "_1\\.readingQuality.*\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[trunc|TRUNC]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.localdate, 'DDD'\\)\\s*FROM.*");
            // Assert that the formula and the aggregation function is applied to the requirements' value in the select clause
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[sum|SUM]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.value\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[avg|AVG]\\(rid" + temperatureRequirementId + "_" + deliverableId + "_1\\.value\\).*FROM.*");
            assertThat(deliverableWithClauseSql).matches(".*[group by trunc|GROUP BY TRUNC]\\(rid" + volumeRequirementId + "_" + deliverableId + "_1\\.localdate.*, 'DDD'\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MEGA_JOULE_DAILY_MRID) + "'.*");
            /* Assert that the overall select statement selects the value from the deliverable. */
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + deliverableId + "_1\\.value.*");
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

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase, amrIdBase).create();
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
    }

    private void activateMeter() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        this.temperatureChannel = this.meterActivation.getChannelsContainer().createChannel(CELCIUS_15min);
        this.pressureChannel = this.meterActivation.getChannelsContainer().createChannel(PRESSURE_15min);
        this.volumeChannel = this.meterActivation.getChannelsContainer().createChannel(VOLUME_15min);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}
