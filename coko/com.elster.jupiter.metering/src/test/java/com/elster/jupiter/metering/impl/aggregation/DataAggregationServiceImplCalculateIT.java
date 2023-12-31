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
import com.elster.jupiter.orm.OrmService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (08:57)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateIT {

    private static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType fifteenMinutesNetConsumption;
    private static ReadingType monthlyNetConsumption;
    private static ReadingType fifteenMinuteskWhForward;
    private static ReadingType fifteenMinuteskWhReverse;
    private static ReadingType thirtyMinuteskWhReverse;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static MeterRole DEFAULT_METER_ROLE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static Instant feb1st2016 = Instant.ofEpochMilli(1454281200000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static MeteringDataModelService dataModelService;
    private static Thesaurus thesaurus;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));
    private MetrologyContract contract;
    private UsagePoint usagePoint;
    private UsagePointMetrologyConfiguration configuration;
    private long productionRequirementId;
    private long consumptionRequirementId;
    private long netConsumptionDeliverableId;
    private SqlBuilder consumptionWithClauseBuilder1;
    private SqlBuilder productionWithClauseBuilder1;
    private SqlBuilder netConsumptionWithClauseBuilder1;
    private SqlBuilder consumptionWithClauseBuilder2;
    private SqlBuilder productionWithClauseBuilder2;
    private SqlBuilder netConsumptionWithClauseBuilder2;
    private SqlBuilder selectClauseBuilder1;
    private SqlBuilder selectClauseBuilder2;
    private SqlBuilder completeSqlBuilder;
    private Meter meter1;

    private Meter meter2;

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
                            "0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0",   // no macro period, measuring period =  15 min, primary metered, reverse (kWh)
                            "0.0.5.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0",   // no macro period, measuring period =  30 min, primary metered, reverse (kWh)
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

    @After
    public void clearCache() {
        injector.getInstance(OrmService.class).invalidateCache("NLS", "NLS_KEY");
    }

    private static MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
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
                injector.getInstance(Clock.class),
                meteringService,
                mock(CalendarService.class),
                mock(CustomPropertySetService.class),
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplCalculateIT::getSqlBuilderFactory,
                () -> new VirtualFactoryImpl(dataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinuteskWhForward = getMeteringService().getReadingType("0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0").get();
            fifteenMinuteskWhReverse = getMeteringService().getReadingType("0.0.2.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0").get();
            thirtyMinuteskWhReverse = getMeteringService().getReadingType("0.0.5.4.19.2.12.0.0.0.0.0.0.0.0.3.72.0").get();
            fifteenMinutesNetConsumption = getMeteringService().getReadingType(FIFTEEN_MINS_NET_CONSUMPTION_MRID).get();
            monthlyNetConsumption = getMeteringService().getReadingType(MONTHLY_NET_CONSUMPTION_MRID).get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurposeAndRole() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName() + ".description");
            when(description.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateIT.class.getSimpleName());
            when(description.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(description.getLayer()).thenReturn(Layer.DOMAIN);
            METROLOGY_PURPOSE = getMetrologyConfigurationService().createMetrologyPurpose(description, description);
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
        this.consumptionWithClauseBuilder1 = new SqlBuilder();
        this.productionWithClauseBuilder1 = new SqlBuilder();
        this.netConsumptionWithClauseBuilder1 = new SqlBuilder();
        this.consumptionWithClauseBuilder2 = new SqlBuilder();
        this.productionWithClauseBuilder2 = new SqlBuilder();
        this.netConsumptionWithClauseBuilder2 = new SqlBuilder();
        this.selectClauseBuilder1 = new SqlBuilder();
        this.selectClauseBuilder2 = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + consumptionRequirementId + ".*1"), any(Optional.class), anyVararg())).thenReturn(this.consumptionWithClauseBuilder1);
        when(clauseAwareSqlBuilder.with(matches("rid" + consumptionRequirementId + ".*2"), any(Optional.class), anyVararg())).thenReturn(this.consumptionWithClauseBuilder2);
        when(clauseAwareSqlBuilder.with(matches("rid" + productionRequirementId + ".*1"), any(Optional.class), anyVararg())).thenReturn(this.productionWithClauseBuilder1);
        when(clauseAwareSqlBuilder.with(matches("rid" + productionRequirementId + ".*2"), any(Optional.class), anyVararg())).thenReturn(this.productionWithClauseBuilder2);
        when(clauseAwareSqlBuilder.with(matches("rod" + netConsumptionDeliverableId + ".*1"), any(Optional.class), anyVararg())).thenReturn(this.netConsumptionWithClauseBuilder1);
        when(clauseAwareSqlBuilder.with(matches("rod" + netConsumptionDeliverableId + ".*2"), any(Optional.class), anyVararg())).thenReturn(this.netConsumptionWithClauseBuilder2);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder1, this.selectClauseBuilder2);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
    }

    private static SearchService mockSearchService() {
        SearchService searchService = mock(SearchService.class);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(any())).thenReturn(Optional.of(searchDomain));
        return searchService;
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
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (15m kWh) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    @Transactional
    public void simplestNetConsumptionOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("simplestNetConsumptionOfProsumer");
        this.setupUsagePoint("simplestNetConsumptionOfProsumer");
        this.activateMeterWithAll15MinChannels(DEFAULT_METER_ROLE);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumptionOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);
        this.contract = configuration.addMetrologyContract(METROLOGY_PURPOSE);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        FullySpecifiedReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhReverse);
        this.productionRequirementId = production.getId();
        System.out.println("simplestNetConsumptionOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("simplestNetConsumptionOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.contract.newReadingTypeDeliverable("consumption", fifteenMinutesNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.plus(
                                builder.requirement(production),
                                builder.requirement(consumption)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("simplestNetConsumptionOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016.plusSeconds(20));

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
            assertThat(consumptionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp as timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder1.getText().replace("\n", " ");
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
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (monthly kWh) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     *
     * @see #simplestNetConsumptionOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.activateMeterWithAll15MinChannels(DEFAULT_METER_ROLE);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumptionOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        FullySpecifiedReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhReverse);
        this.productionRequirementId = production.getId();
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        ReadingTypeDeliverableBuilder builder = this.contract.newReadingTypeDeliverable("consumption", monthlyNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.plus(
                                builder.requirement(production),
                                builder.requirement(consumption)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyNetConsumptionBasedOn15MinValuesOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, feb1st2016);

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
            assertThat(consumptionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*FROM.*\\(SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*\\) realrod" + netConsumptionDeliverableId + "_1.*");
            // Assert that both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*FROM.*\\(SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*FROM.*\\(SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            /* Assert that the select statement sums up the values from the real rod,
             * using a group by construct that truncs the localdate to month. */
            assertThat(this.netConsumptionWithClauseBuilder1.getText()).matches("SELECT.*[sum|SUM]\\(realrod" + netConsumptionDeliverableId + "_1\\.value\\).*FROM.*\\(SELECT.*\\) realrod" + netConsumptionDeliverableId + "_1 [group by|GROUP BY].*");
            assertThat(this.netConsumptionWithClauseBuilder1.getText()).matches(".*[trunc|TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\).*");
            assertThat(this.netConsumptionWithClauseBuilder1.getText()).matches(".*[group by trunc|GROUP BY TRUNC]\\(realrod" + netConsumptionDeliverableId + "_1\\.localdate, 'MONTH'\\)");
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder1.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
        }
    }

    /**
     * Similar to the monthly case above but the two requirements
     * produce different but compatible intervals so that one
     * of them needs to be aggregated first and then aggregated
     * again on the deliverable level:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (monthly kWh) ::= A- + (A+ * 2)
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 30 min kWh
     * In other words, A- need aggregation to 30min first
     * to be able to add to the 30min values produces by A+
     * The sum of both values then need to be aggregated to the requested monthly level.
     *
     * @see #monthlyNetConsumptionBasedOn15MinValuesOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer");
        this.activateMeterWithAll15And30MinChannels();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumptionOfProsumer", ELECTRICITY).create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        FullySpecifiedReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(thirtyMinuteskWhReverse);
        this.productionRequirementId = production.getId();
        System.out.println("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        ReadingTypeDeliverableBuilder builder = this.contract.newReadingTypeDeliverable("consumption", monthlyNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(builder.plus(
                        builder.requirement(consumption),
                        builder.multiply(
                                builder.requirement(production),
                                builder.constant(BigDecimal.valueOf(2L)))));


        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, feb1st2016);

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
            assertThat(consumptionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the with clause for the production requirement is NOT aggregated
            String productionWithSelectClause = this.productionWithClauseBuilder1.getText();
            assertThat(productionWithSelectClause).doesNotMatch(".*[sum|SUM]\\(value\\).*");
            assertThat(productionWithSelectClause).doesNotMatch(".*[trunc|TRUNC]\\(localdate, 'MONTH'\\).*");
            assertThat(productionWithSelectClause).doesNotMatch(".*[group by trunc|GROUP BY TRUNC]\\(localdate, 'MONTH'\\).*");
            // Assert that the with clause for the consumption requirement is aggregated to 30min values
            String consumptionWithSelectClause = this.consumptionWithClauseBuilder1.getText();
            assertThat(consumptionWithSelectClause).matches(".*[sum|SUM]\\(value\\).*");
            assertThat(consumptionWithSelectClause).matches(".*[group by floor|GROUP BY FLOOR]\\(rawdata\\.timestamp/1800000\\).*");
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*[max|MAX]\\(realrod" + netConsumptionDeliverableId + "_1\\.timestamp\\).*");
            // Assert that both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches(".*\\(rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ \\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\s\\*\\s*\\?\\s*\\)\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches(".*JOIN rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder1.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + netConsumptionDeliverableId + "_1\\.value, rod" + netConsumptionDeliverableId + "_1\\.localdate, rod" + netConsumptionDeliverableId + "_1\\.timestamp.*");
        }
    }

    /**
     * Tests the simplest case with multiple meter activations:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (15m kWh) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * Feb 1st 2016 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel for all meter activations.
     */
    @Test
    @Transactional
    public void simplestNetConsumptionOfProsumerWithMultipleMeterActivations() {
        DataAggregationService service = this.testInstance();
        this.setupMeters("simplestNetConsumptionOfProsumerWithMultipleMeterActivations");
        this.setupUsagePoint("simplestNetConsumptionOfProsumerWithMultipleMeterActivations");
        this.activateMetersWithAll15MinChannels(DEFAULT_METER_ROLE);

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumptionOfProsumer", ELECTRICITY)
                .create();
        this.configuration.addMeterRole(DEFAULT_METER_ROLE);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        FullySpecifiedReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", DEFAULT_METER_ROLE)
                .withReadingType(fifteenMinuteskWhReverse);
        this.productionRequirementId = production.getId();
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        ReadingTypeDeliverableBuilder builder = this.contract.newReadingTypeDeliverable("consumption", fifteenMinutesNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.plus(
                                builder.requirement(production),
                                builder.requirement(consumption)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, feb1st2016);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            // First meter activation
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Second meter activation
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + consumptionRequirementId + ".*" + netConsumptionDeliverableId + ".*2"),
                            any(Optional.class),
                            anyVararg());
            assertThat(consumptionWithClauseBuilder2.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + productionRequirementId + ".*" + netConsumptionDeliverableId + ".*2"),
                            any(Optional.class),
                            anyVararg());
            assertThat(productionWithClauseBuilder2.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + netConsumptionDeliverableId + ".*2"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline in the first meter activation
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp as timestamp,.*");
            // Assert that one of the requirements is used as source for the timeline in the second meter activation
            assertThat(this.netConsumptionWithClauseBuilder2.getText())
                    .matches("SELECT -1 as id, rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_2\\.timestamp as timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause for the first meter activation
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value \\+ rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.value\\).*");
            // Assert that one of both requirements' values are added up in the select clause for the second meter activation
            assertThat(this.netConsumptionWithClauseBuilder2.getText())
                    .matches("SELECT.*\\(rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_2\\.value \\+ rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_2\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp (first meter activation
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            assertThat(this.netConsumptionWithClauseBuilder2.getText())
                    .matches("SELECT.*JOIN rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_2 ON rid" + consumptionRequirementId + "_" + netConsumptionDeliverableId + "_2\\.timestamp = rid" + productionRequirementId + "_" + netConsumptionDeliverableId + "_2\\.timestamp.*");
            verify(clauseAwareSqlBuilder, times(2)).select();   // calling select twice has the effect that the 2nd select is unioned with the first, other test classes are focussing on that
            // Assert that the overall select statement for first meter activation selects the target reading type
            String overallSelectWithoutNewlines1 = this.selectClauseBuilder1.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines1).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines1).matches(".*rod" + netConsumptionDeliverableId + "_1\\.value, rod" + netConsumptionDeliverableId + "_1\\.localdate, rod" + netConsumptionDeliverableId + "_1\\.timestamp.*");
            // Assert that the overall select statement for second meter activation selects the target reading type
            String overallSelectWithoutNewlines2 = this.selectClauseBuilder2.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines2).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines2).matches(".*rod" + netConsumptionDeliverableId + "_2\\.value, rod" + netConsumptionDeliverableId + "_2\\.localdate, rod" + netConsumptionDeliverableId + "_2\\.timestamp.*");
        }
    }

    /**
     * Tests the case with multiple meter roles:
     * Metrology configuration
     * roles:
     * {@link DefaultMeterRole#CONSUMPTION} & {@link DefaultMeterRole#PRODUCTION}
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (15m kWh) ::= A- + A+
     * Device:
     * meter activations for consumption
     * Jan 1st 2016 -> forever
     * A- -> 15 min kWh
     * meter activations for production
     * Jan 1st 2016 -> forever
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel for all meter roles.
     */
    @Test
    @Transactional
    public void netConsumptionOfProsumerWithMultipleMeterRoles() {
        MeterRole consumptionMeterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.CONSUMPTION);
        MeterRole productionMeterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.PRODUCTION);
        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("netConsumptionOfProsumerWithMultipleMeterRoles", ELECTRICITY).create();
        this.configuration.addMeterRole(consumptionMeterRole);
        this.configuration.addMeterRole(productionMeterRole);

        DataAggregationService service = this.testInstance();
        this.setupMeters("netConsumptionOfProsumerWithMultipleMeterRoles");
        this.setupUsagePoint("netConsumptionOfProsumerWithMultipleMeterRoles");
        this.activateMetersWithAll15MinChannels(consumptionMeterRole, productionMeterRole);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", consumptionMeterRole).withReadingType(fifteenMinuteskWhForward);
        this.consumptionRequirementId = consumption.getId();
        FullySpecifiedReadingTypeRequirement production = this.configuration.newReadingTypeRequirement("A+", productionMeterRole).withReadingType(fifteenMinuteskWhReverse);
        this.productionRequirementId = production.getId();
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::CONSUMPTION_REQUIREMENT_ID = " + consumptionRequirementId);
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::PRODUCTION_REQUIREMENT_ID = " + productionRequirementId);

        // Setup configuration deliverables
        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        ReadingTypeDeliverableBuilder builder = this.contract.newReadingTypeDeliverable("consumption", fifteenMinutesNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.plus(
                                builder.requirement(production),
                                builder.requirement(consumption)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("simplestNetConsumptionOfProsumerWithMultipleMeterActivations::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + this.consumptionRequirementId + ".*" + this.netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(this.consumptionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + this.productionRequirementId + ".*" + this.netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(this.productionWithClauseBuilder1.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + this.netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT -1 as id, rid" + this.productionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.timestamp as timestamp.*");
            // Assert that both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*\\(rid" + this.productionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.value \\+ rid" + this.consumptionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder1.getText())
                    .matches("SELECT.*JOIN rid" + this.consumptionRequirementId + "_" + this.netConsumptionDeliverableId + "_1 ON rid" + this.consumptionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.timestamp = rid" + this.productionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement containts the target reading type
            String overallSelectWithoutNewlines1 = this.selectClauseBuilder1.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines1).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
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
        this.meter1 = spy(mdc.newMeter(amrIdBase, amrIdBase).create());
        when(meter1.getState(any(Instant.class))).thenReturn(Optional.of(deviceState));
        when(deviceState.getStage()).thenReturn(Optional.of(deviceStage));
        when(deviceStage.getName()).thenReturn(OPERATIONAL_DEVICE_STAGE_KEY);
    }

    private void setupMeters(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter1 = spy(mdc.newMeter(amrIdBase + "-1", amrIdBase + "-1").create());
        this.meter2 = spy(mdc.newMeter(amrIdBase + "-2", amrIdBase + "-2").create());
        when(meter1.getState(any(Instant.class))).thenReturn(Optional.of(deviceState));
        when(meter2.getState(any(Instant.class))).thenReturn(Optional.of(deviceState));
        when(deviceState.getStage()).thenReturn(Optional.of(deviceStage));
        when(deviceStage.getName()).thenReturn(OPERATIONAL_DEVICE_STAGE_KEY);
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
    }

    private void activateMeterWithAll15MinChannels(MeterRole meterRole) {
        MeterActivation meterActivation = this.usagePoint.activate(this.meter1, meterRole, feb1st2016);
        meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWhReverse);
        meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
    }

    private void activateMetersWithAll15MinChannels(MeterRole meterRole) {
        MeterActivation meterActivation1 = this.usagePoint.activate(this.meter1, meterRole, feb1st2016);
        MeterActivation meterActivation2 = this.usagePoint.activate(this.meter2, meterRole, feb1st2016.plusSeconds(60));
        meterActivation1.getChannelsContainer().createChannel(fifteenMinuteskWhReverse);
        meterActivation1.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
        meterActivation2.getChannelsContainer().createChannel(fifteenMinuteskWhReverse);
        meterActivation2.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
    }

    private void activateMetersWithAll15MinChannels(MeterRole consumptionMeterRole, MeterRole productionMeterRole) {
        MeterActivation consumptionMA = this.usagePoint.activate(this.meter1, consumptionMeterRole, jan1st2016);
        MeterActivation productionMA = this.usagePoint.activate(this.meter2, productionMeterRole, jan1st2016);
        consumptionMA.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
        productionMA.getChannelsContainer().createChannel(fifteenMinuteskWhReverse);
    }

    private void activateMeterWithAll15And30MinChannels() {
        MeterActivation meterActivation = this.usagePoint.activate(this.meter1, feb1st2016);
        meterActivation.getChannelsContainer().createChannel(thirtyMinuteskWhReverse);
        meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWhForward);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }
}
