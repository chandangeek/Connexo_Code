/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
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
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
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
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.DayMonthTime;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method
 * for reading types that relate to gas.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-18 (14:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateGasIT {

    private static final String FIFTEEN_MINS_GAS_VOLUME_M3_MRID = "0.0.2.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0";
    private static final String FIFTEEN_MINS_GAS_VOLUME_KWH_MRID = "0.0.2.4.1.7.58.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MONTHLY_GAS_VOLUME_M3_MRID = "13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0";
    private static final String MONTHLY_GAS_VOLUME_KWH_MRID = "13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.3.72.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static BundleContext bundleContext;
    private static ReadingType fifteenMinutesGasCubicMeter;
    private static ReadingType fifteenMinutesGas_kWh;
    private static ReadingType monthlyGasCubicMeter;
    private static ReadingType monthlyGas_kWh;
    private static ServiceCategory GAS;
    private static SearchService searchService;
    private static SearchDomain searchDomain;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static Instant feb1st2016 = Instant.ofEpochMilli(1454281200000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    private MetrologyContract contract;
    private UsagePoint usagePoint;
    private UsagePointMetrologyConfiguration configuration;
    private long consumptionRequirementId;
    private long consumptionDeliverableId;
    private SqlBuilder consumptionRequirementWithClauseBuilder;
    private SqlBuilder consumptionDeliverableWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;

    @Mock
    private Thesaurus thesaurus;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
            bind(SearchService.class).toInstance(searchService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        setupBundleContext();
        setupServices();
        setupReadingTypes();
        setupMetrologyPurpose();
        GAS = getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        setupGasDayOptions();
        setupDefaultUsagePointLifeCycle();
    }

    private static void setupBundleContext() {
        bundleContext = mock(BundleContext.class);
        when(bundleContext.getProperty("com.elster.jupiter.location.template")).thenReturn("#ccod,#cnam,#adma,#loc,#subloc,#styp,#snam,#snum,#etyp,#enam,#enum,#addtl,#zip,#locale");
        when(bundleContext.getProperty("com.elster.jupiter.location.template.mandatoryfields")).thenReturn("#adma,#loc,#styp,#snam,#snum");
    }

    private static void setupServices() {
        searchDomain = mock(SearchDomain.class);
        searchService = mock(SearchService.class);
        when(searchService.findDomain(anyString())).thenReturn(Optional.of(searchDomain));
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(
                            FIFTEEN_MINS_GAS_VOLUME_M3_MRID,
                            FIFTEEN_MINS_GAS_VOLUME_KWH_MRID,
                            MONTHLY_GAS_VOLUME_M3_MRID,
                            MONTHLY_GAS_VOLUME_KWH_MRID
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

    @After
    public void clearCache() {
        injector.getInstance(OrmService.class).invalidateCache("NLS", "NLS_KEY");
    }

    private static ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }

    private static ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(ServerMetrologyConfigurationService.class);
    }

    private static SqlBuilderFactory getSqlBuilderFactory() {
        return sqlBuilderFactory;
    }

    private static DataAggregationService getDataAggregationService() {
        ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
        return new DataAggregationServiceImpl(
                mock(CustomPropertySetService.class),
                meteringService,
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplCalculateGasIT::getSqlBuilderFactory,
                VirtualFactoryImpl::new,
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinutesGasCubicMeter = getMeteringService().getReadingType(FIFTEEN_MINS_GAS_VOLUME_M3_MRID).get();
            fifteenMinutesGas_kWh = getMeteringService().getReadingType(FIFTEEN_MINS_GAS_VOLUME_KWH_MRID).get();
            monthlyGasCubicMeter = getMeteringService().getReadingType(MONTHLY_GAS_VOLUME_M3_MRID).get();
            monthlyGas_kWh = getMeteringService().getReadingType(MONTHLY_GAS_VOLUME_KWH_MRID).get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurpose() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplCalculateGasIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateGasIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplCalculateGasIT.class.getSimpleName() + ".description");
            when(description.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateGasIT.class.getSimpleName());
            when(description.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(description.getLayer()).thenReturn(Layer.DOMAIN);
            METROLOGY_PURPOSE = getMetrologyConfigurationService().createMetrologyPurpose(name, description);
            ctx.commit();
        }
    }

    private static void setupGasDayOptions() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getMeteringService().createGasDayOptions(DayMonthTime.from(MonthDay.of(Month.OCTOBER, 1), LocalTime.of(17, 0)));
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
        this.consumptionRequirementWithClauseBuilder = new SqlBuilder();
        this.consumptionDeliverableWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + consumptionRequirementId + ".*1"), any(Optional.class), anyVararg()))
                .thenReturn(this.consumptionRequirementWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + consumptionDeliverableId + ".*1"), any(Optional.class), anyVararg()))
                .thenReturn(this.consumptionDeliverableWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
    }

    @After
    public void resetSqlBuilders() {
        reset(sqlBuilderFactory);
        reset(clauseAwareSqlBuilder);
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     * requirements:
     * C ::= any m3 (aka consumption)
     * deliverables:
     * Conso (15m kWh) ::= C * 10.4167 (constant determined as 1 m3 gas represents 37.5MJoule, 1 Joule equals 0.0000002777...kWh)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * C -> 15 min kWh
     * In other words, simple multiplication of constant and 1 requirements that is provided
     * by exactly one matching channel with a single meter activation
     * without truncation that requires gas day start options.
     */
    @Test
    @Transactional
    public void simple15Mins() {
        MeterRole defaultMeterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        DataAggregationService service = this.testInstance();
        this.setupMeter("simple15Mins");
        this.setupUsagePoint("simple15Mins");
        this.activateMeterWith15min_m3_Channel(defaultMeterRole);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simple15Mins", GAS)
                .create();
        this.configuration.addMeterRole(defaultMeterRole);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("C", defaultMeterRole)
                .withReadingType(fifteenMinutesGasCubicMeter);
        this.consumptionRequirementId = consumption.getId();
        System.out.println("simple15Mins::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("consumption", fifteenMinutesGas_kWh, Formula.Mode.EXPERT);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.multiply(
                                builder.constant(BigDecimal.valueOf(10.4167d)),
                                builder.requirement(consumption)));

        this.consumptionDeliverableId = netConsumption.getId();
        System.out.println("simple15Mins::NET_CONSUMPTION_DELIVERABLE_ID = " + this.consumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(netConsumption);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + consumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionRequirementWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + consumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the consumption requirement is used as source for the timeline
            assertThat(this.consumptionDeliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + consumptionRequirementId + "_" + consumptionDeliverableId + "_1\\.timestamp,.*");
            // Assert that the consumption requirements' values is multiplied with the constant in the select clause
            assertThat(this.consumptionDeliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(\\s*\\?\\s*\\* rid" + consumptionRequirementId + "_" + consumptionDeliverableId + "_1\\.value\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_GAS_VOLUME_KWH_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + consumptionDeliverableId + "_1\\.value, rod" + consumptionDeliverableId + "_1\\.localdate, rod" + consumptionDeliverableId + "_1\\.timestamp.*");
        }
    }

    /**
     * Aggregate 15 min values to monthly level.
     * Metrology configuration
     * requirements:
     * C ::= any kWh (aka consumption)
     * deliverables:
     * Conso (monthly kWh) ::= C
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * C -> 15 min kWh
     * In other words, simple multiplication of constant and 1 requirements that is provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    @Transactional
    public void monthlyValuesFrom15minValues() {
        MeterRole defaultMeterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyValuesFrom15minValues");
        this.setupUsagePoint("monthlyValuesFrom15minValues");
        this.activateMeterWith15min_kWh_Channel(defaultMeterRole);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("monthlyValuesFrom15minValues", GAS)
                .create();
        this.configuration.addMeterRole(defaultMeterRole);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("C", defaultMeterRole)
                .withReadingType(fifteenMinutesGas_kWh);
        this.consumptionRequirementId = consumption.getId();
        System.out.println("monthlyValuesFrom15minValues::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("consumption", monthlyGas_kWh, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption = builder.build(builder.requirement(consumption));

        this.consumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyValuesFrom15minValues::NET_CONSUMPTION_DELIVERABLE_ID = " + this.consumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(netConsumption);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + consumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionRequirementWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + consumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the consumption requirement is used as source for the timeline
            assertThat(this.consumptionDeliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + consumptionRequirementId + "_" + consumptionDeliverableId + "_1\\.timestamp,.*");
            verify(clauseAwareSqlBuilder).select();
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            // Assert that the overall select statement selects the target reading type
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_GAS_VOLUME_KWH_MRID) + "'.*");
            // Assert that the overall select statement truncates to monthly level and takes the gas day start options into account.
            assertThat(overallSelectWithoutNewlines).matches(".*\\(TRUNC\\(rod" + consumptionDeliverableId + "_1.localdate - INTERVAL '17' HOUR, 'MONTH'\\) \\+ INTERVAL '17' HOUR\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*GROUP BY \\(TRUNC\\(rod" + consumptionDeliverableId + "_1.localdate - INTERVAL '17' HOUR, 'MONTH'\\) \\+ INTERVAL '17' HOUR\\).*");
            // Assert that the overall select statement sums the values and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*SUM\\(rod" + consumptionDeliverableId + "_1\\.value\\).*");
        }
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase, amrIdBase).create();
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016)
                .create();
    }

    private void activateMeterWith15min_m3_Channel(MeterRole meterRole) {
        MeterActivation meterActivation = this.usagePoint.activate(this.meter, meterRole, jan1st2016);
        meterActivation.getChannelsContainer().createChannel(fifteenMinutesGasCubicMeter);
    }

    private void activateMeterWith15min_kWh_Channel(MeterRole meterRole) {
        MeterActivation meterActivation = this.usagePoint.activate(this.meter, meterRole, jan1st2016);
        meterActivation.getChannelsContainer().createChannel(fifteenMinutesGas_kWh);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}
