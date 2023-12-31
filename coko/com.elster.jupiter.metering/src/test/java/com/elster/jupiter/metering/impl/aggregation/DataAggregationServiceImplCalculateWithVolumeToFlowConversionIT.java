/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
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
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.http.whiteboard.TokenService;
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
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
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
import org.osgi.service.http.HttpService;

import java.math.BigDecimal;
import java.time.Clock;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method
 * when volume to flow conversion (WattHour -> Watt) is required.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-03 (16:57)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateWithVolumeToFlowConversionIT {

    public static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.38.0";
    public static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.38.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType fifteenMinutesNetConsumption;
    private static ReadingType monthlyNetConsumption;
    private static ReadingType fifteenMinuteskWhForward;
    private static ReadingType fifteenMinuteskWReverse;
    private static ReadingType hourlykWReverse;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static MeterRole DEFAULT_METER_ROLE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static MeteringDataModelService dataModelService;
    private static Thesaurus thesaurus;
    private long productionRequirementId;
    private long consumptionRequirementId;

    private long netConsumptionDeliverableId;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));
    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private MetrologyContract contract;
    private SqlBuilder consumptionWithClauseBuilder;
    private SqlBuilder productionWithClauseBuilder;
    private SqlBuilder netConsumptionWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;
    private MeterActivation meterActivation;
    private Channel production15MinChannel;
    private Channel production60MinChannel;
    private Channel consumption15MinChannel;

    private UsagePoint usagePoint;

    @Mock
    private static State deviceState;
    @Mock
    private static Stage deviceStage;

    private static final String OPERATIONAL_DEVICE_STAGE_KEY = "mtr.enddevicestage.operational";

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
            bind(SearchService.class).toInstance(mockSearchService());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(TokenService.class).toInstance(mock(TokenService.class));
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
                            "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0",    // no macro period, measuring period =  15 min, primary metered, forward (kWh)
                            "0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.38.0",   // no macro period, measuring period =  15 min, primary metered, reverse (kW)
                            "0.0.7.4.19.2.12.0.0.0.0.0.0.0.0.3.38.0",   // no macro period, measuring period =  60 min, primary metered, reverse (kW)
                            FIFTEEN_MINS_NET_CONSUMPTION_MRID,          // no macro period, measuring period =  15 min, primary metered, net (kWh)
                            MONTHLY_NET_CONSUMPTION_MRID                // macro period: monthly, measuring period: none, primary metered, net (kWh)
                    ),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new UserModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
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
                    new BasicPropertiesModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new WebServicesModule(),
                    new AuditServiceModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(EndPointConfigurationService.class);
            injector.getInstance(WebServicesService.class);
            injector.getInstance(AuditService.class);
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
                injector.getInstance(Clock.class),
                meteringService,
                mock(CalendarService.class),
                mock(CustomPropertySetService.class),
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplCalculateWithVolumeToFlowConversionIT::getSqlBuilderFactory,
                () -> new VirtualFactoryImpl(dataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinuteskWhForward = getMeteringService().getReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0").get();
            fifteenMinuteskWReverse = getMeteringService().getReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.38.0").get();
            hourlykWReverse = getMeteringService().getReadingType("0.0.7.4.19.2.12.0.0.0.0.0.0.0.0.3.38.0").get();
            fifteenMinutesNetConsumption = getMeteringService().getReadingType(FIFTEEN_MINS_NET_CONSUMPTION_MRID).get();
            monthlyNetConsumption = getMeteringService().getReadingType(MONTHLY_NET_CONSUMPTION_MRID).get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurposeAndRole() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn("DASImplCalculateWithVolumeToFlowConversionIT");
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn("DASImplCalculateWithVolumeToFlowConversionIT.description");
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
        this.consumptionWithClauseBuilder = new SqlBuilder();
        this.productionWithClauseBuilder = new SqlBuilder();
        this.netConsumptionWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
    }

    private void initializeSqlBuilders() {
        when(clauseAwareSqlBuilder.with(matches("rid" + consumptionRequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.consumptionWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + productionRequirementId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.productionWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + netConsumptionDeliverableId + ".*"), any(Optional.class), anyVararg())).thenReturn(this.netConsumptionWithClauseBuilder);
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
     * Tests the simplest case of volume to flow unit conversion:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any W  with flow = reverse (aka production)
     * deliverables:
     * netConsumption (15m kW) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kW
     * In other words, the 2 requirements are provided by exactly
     * one matching channel from a single meter activation
     * but the kWh channel (A-) needs to be converted to kW
     * before summing it with the kW channel (A+).
     */
    @Test
    @Transactional
    public void simplestNetConsumptionOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("simplestNetConsumptionOfProsumer");
        this.setupUsagePoint("simplestNetConsumptionOfProsumer");
        this.activateMeterWithAll15MinChannels();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumptionOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);
        this.contract = configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Setup configuration requirements
        ReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        ReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWReverse);
        this.productionRequirementId = production.getId();
        System.out.println("simplestNetConsumptionOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("simplestNetConsumptionOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = newDeliveryBuilder("consumption", contract, fifteenMinutesNetConsumption);
        ReadingTypeDeliverable netConsumption =
                builder.build(builder.plus(
                        builder.requirement(production),
                        builder.requirement(consumption)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("simplestNetConsumptionOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp as timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ \\(rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\* 4\\)\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + netConsumptionDeliverableId + "_1\\.value, rod" + netConsumptionDeliverableId + "_1\\.localdate, rod" + netConsumptionDeliverableId + "_1\\.timestamp.*");
        }
    }

    /**
     * Simular to the simplest case above but the deliverable
     * is configured to produce monthly values:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any W  with flow = reverse (aka production)
     * deliverables:
     * netConsumption (monthly kW) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kW
     * In other words, the 2 requirements are provided by exactly
     * one matching channel from a single meter activation
     * but the kWh channel (A-) needs to be converted to kW
     * before summing it with the kW channel (A+)
     * Both are aggregated to monthly level at the definition level
     * so no aggregation is needed on deliverable level.
     *
     * @see #simplestNetConsumptionOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.activateMeterWithAll15MinChannels();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("monthlyNetConsumptionBasedOn15MinValuesOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);
        this.contract = configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Setup configuration requirements
        ReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        ReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWReverse);
        this.productionRequirementId = production.getId();
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = newDeliveryBuilder("consumption", contract, monthlyNetConsumption);
        ReadingTypeDeliverable netConsumption =
                builder.build(builder.plus(
                        builder.requirement(production),
                        builder.requirement(consumption)));


        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the with clause for the the production requirement does not contain aggregation constructs
            String productionWithSelectClause = this.productionWithClauseBuilder.getText();
            assertThat(productionWithSelectClause).doesNotMatch(".*TRUNC.*");
            // Assert that the with clause for the the consumption requirement does not contain aggregation constructs
            String consumptionWithSelectClause = this.consumptionWithClauseBuilder.getText();
            assertThat(consumptionWithSelectClause).doesNotMatch(".*TRUNC.*");
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*FROM.*\\(SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp as timestamp.*\\) realrod" + netConsumptionDeliverableId + "_1.*");
            // Assert that both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ \\(rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\* 4\\)\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            /* Assert that the select statement sums up the values
             * using a group by construct that truncs the localdate to month. */
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[avg|AVG]\\(realrod" + netConsumptionDeliverableId + "_1\\.value\\).*");
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[trunc|TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\).*");
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[group by trunc|GROUP BY TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
        }
    }

    /**
     * Simular to the monthly case above but the two requirements
     * produce different but compatible intervals so that one
     * of them needs to be aggregated first and then aggregated
     * again on the deliverable level:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any W  with flow = reverse (aka production)
     * deliverables:
     * netConsumption (monthly kW) ::= A- + (A+ * 2)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 60 min kW
     * In other words, A- need aggregation to 60 min values
     * to be able to sum them with the A+ values.
     * The summed up values can then be aggregated to the requested monthly level.
     * A- must be converted to kW while aggregating it to 60 min level.
     *
     * @see #monthlyNetConsumptionBasedOn15MinValuesOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer");
        this.activateMeterWith15And60MinChannels();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);
        this.contract = configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Setup configuration requirements
        ReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        ReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(hourlykWReverse);
        this.productionRequirementId = production.getId();
        System.out.println("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = newDeliveryBuilder("consumption", contract, monthlyNetConsumption);
        ReadingTypeDeliverable netConsumption =
                builder.build(builder.plus(
                        builder.requirement(consumption),
                        builder.multiply(
                                builder.requirement(production),
                                builder.constant(BigDecimal.valueOf(2L)))));


        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyNetConsumptionBasedOn15And60MinValuesOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the with clause for the the production requirement does not contain aggregation constructs
            String productionWithSelectClause = this.productionWithClauseBuilder.getText();
            assertThat(productionWithSelectClause).doesNotMatch(".*TRUNC.*");
            String consumptionWithSelectClause = this.consumptionWithClauseBuilder.getText();
            // Assert that the with clause for the the consumption requirement contains aggregation constructs for hourly level
            assertThat(consumptionWithSelectClause).matches(".*[trunc|TRUNC]\\(rawdata\\.localdate, 'HH'\\).*");
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*FROM.*\\(SELECT -1 as id, rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp as timestamp.*\\) realrod" + netConsumptionDeliverableId + "_1.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(\\(rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\s\\*\\s4\\)\\s\\+\\s*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\s*\\*\\s*\\?\\s*\\)\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            /* Assert that the select statement sums up the values
             * using a group by construct that truncs the localdate to month. */
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[avg|AVG]\\(realrod" + netConsumptionDeliverableId + "_1\\.value\\).*");
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[trunc|TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\).*");
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches(".*[group by trunc|GROUP BY TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
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
        this.meter = spy(mdc.newMeter(amrIdBase, amrIdBase).create());
        when(meter.getState(any(Instant.class))).thenReturn(Optional.of(deviceState));
        when(deviceState.getStage()).thenReturn(Optional.of(deviceStage));
        when(deviceStage.getName()).thenReturn(OPERATIONAL_DEVICE_STAGE_KEY);
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
    }

    private void activateMeterWithAll15MinChannels() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        this.production15MinChannel = this.meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWReverse);
        this.consumption15MinChannel = this.meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
    }

    private void activateMeterWith15And60MinChannels() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        this.production60MinChannel = this.meterActivation.getChannelsContainer().createChannel(hourlykWReverse);
        this.consumption15MinChannel = this.meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

    private ReadingTypeDeliverableBuilder newDeliveryBuilder(String name, MetrologyContract configuration, ReadingType readingType) {
        return contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);

    }
}
