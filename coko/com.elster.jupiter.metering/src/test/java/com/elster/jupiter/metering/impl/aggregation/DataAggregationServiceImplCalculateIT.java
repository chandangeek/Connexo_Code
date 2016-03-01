package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
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
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.OperationNode;
import com.elster.jupiter.metering.impl.config.Operator;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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

    public static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    public static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType fifteenMinuteskWhForward;
    private static ReadingType fifteenMinuteskWhReverse;
    private static ReadingType thirtyMinuteskWhReverse;
    private static Instant jan1st2015 = Instant.ofEpochMilli(1420070400000L);
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static long PRODUCTION_REQUIREMENT_ID = 97L;
    private static long CONSUMPTION_REQUIREMENT_ID = 98L;
    private static long NET_CONSUMPTION_DELIVERABLE_ID = 99L;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    @Mock
    private MetrologyConfiguration configuration;
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
    private Channel production30MinChannel;
    private Channel consumption15MinChannel;
    private UsagePoint usagePoint;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
        }
    }

    @BeforeClass
    public static void setUp() {
        setupServices();
        setupReadingTypes();
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
                            "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0",    // no macro period, measuring period =  15 min, primary metered, forward
                            "0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.0.72.0",   // no macro period, measuring period =  15 min, primary metered, reverse
                            "0.0.5.1.19.2.12.0.0.0.0.0.0.0.0.0.72.0",   // no macro period, measuring period =  30 min, primary metered, reverse
                            "0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0",    // no macro period, measuring period =  15 min, primary metered, net
                            "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0"    // macro period: monthly, measuring period: none, primary metered, net
                    ),
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
                    new CustomPropertySetsModule()
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

    private static DataAggregationService getDataAggregationService() {
        return new DataAggregationServiceImpl(
                injector.getInstance(ServerMeteringService.class),
                VirtualFactoryImpl::new,
                sqlBuilderFactory);
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinuteskWhForward = getMeteringService().getReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
            fifteenMinuteskWhReverse = getMeteringService().getReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
            thirtyMinuteskWhReverse = getMeteringService().getReadingType("0.0.5.1.19.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void resetSqlBuilder() {
        reset(sqlBuilderFactory);
        reset(clauseAwareSqlBuilder);
        this.consumptionWithClauseBuilder = new SqlBuilder();
        this.productionWithClauseBuilder = new SqlBuilder();
        this.netConsumptionWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + CONSUMPTION_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.consumptionWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + PRODUCTION_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.productionWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + NET_CONSUMPTION_DELIVERABLE_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.netConsumptionWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     *    requirements:
     *       A- ::= any Wh with flow = forward (aka consumption)
     *       A+ ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (15m kWh) ::= A- + A+
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A- -> 15 min kWh
     *           A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    @Transactional
    public void simplestNetConsumptionOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("simplestNetConsumptionOfProsumer");
        this.setupUsagePoint("simplestNetConsumptionOfProsumer");
        this.activateMeterWithAll15MinChannels();

        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getId()).thenReturn(CONSUMPTION_REQUIREMENT_ID);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getId()).thenReturn(PRODUCTION_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getId()).thenReturn(NET_CONSUMPTION_DELIVERABLE_ID);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(
                new OperationNode(
                        Operator.PLUS,
                        new ReadingTypeRequirementNode(production),
                        new ReadingTypeRequirementNode(consumption)))
                .when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        when(consumption.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWhForward));
        when(consumption.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.consumption15MinChannel));
        when(production.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWhReverse));
        when(production.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.production15MinChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + CONSUMPTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + PRODUCTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value \\+ rid98_99_1\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid98_99_1 ON rid98_99_1\\.timestamp = rid97_99_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod99_1\\.value, rod99_1\\.timestamp.*");
        }
    }

    /**
     * Simular to the simplest case above but the deliverable
     * is configured to produce monthly values:
     * Metrology configuration
     *    requirements:
     *       A- ::= any Wh with flow = forward (aka consumption)
     *       A+ ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (monthly kWh) ::= A- + A+
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A- -> 15 min kWh
     *           A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     * @see #simplestNetConsumptionOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15MinValuesOfProsumer");
        this.activateMeterWithAll15MinChannels();

        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getId()).thenReturn(CONSUMPTION_REQUIREMENT_ID);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getId()).thenReturn(PRODUCTION_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getId()).thenReturn(NET_CONSUMPTION_DELIVERABLE_ID);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mockMOnthlyNetConsumptionReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(
                new OperationNode(
                        Operator.PLUS,
                        new ReadingTypeRequirementNode(production),
                        new ReadingTypeRequirementNode(consumption)))
                .when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        when(consumption.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWhForward));
        when(consumption.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.consumption15MinChannel));
        when(production.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWhReverse));
        when(production.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.production15MinChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + CONSUMPTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + PRODUCTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value \\+ rid98_99_1\\.value\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid98_99_1 ON rid98_99_1\\.timestamp = rid97_99_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
            /* Assert that the overall select statement sums up the values
             * from the with clause for the deliverable, using a group by
             * construct that truncs the localdate to month. */
            assertThat(overallSelectWithoutNewlines).matches(".*[sum|SUM]\\(rod99_1\\.value\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod99_1\\.localdate, 'MONTH'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod99_1\\.localdate, 'MONTH'\\).*");
        }
    }

    /**
     * Simular to the monthly case above but the two requirements
     * produce different but compatible intervals so that one
     * of them needs to be aggregated first and then aggregated
     * again on the deliverable level:
     * Metrology configuration
     *    requirements:
     *       A- ::= any Wh with flow = forward (aka consumption)
     *       A+ ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (monthly kWh) ::= A- + (A+ * 2)
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A- -> 15 min kWh
     *           A+ -> 30 min kWh
     * In other words, A+ and A- need aggregation to monthly values
     * before summing up but that achieves the requested monthly level.
     * @see #monthlyNetConsumptionBasedOn15MinValuesOfProsumer()
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer");
        this.setupUsagePoint("monthlyNetConsumptionBasedOn15And30MinValuesOfProsumer");
        this.activateMeterWithAll15And30MinChannels();

        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getId()).thenReturn(CONSUMPTION_REQUIREMENT_ID);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getId()).thenReturn(PRODUCTION_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getId()).thenReturn(NET_CONSUMPTION_DELIVERABLE_ID);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mockMOnthlyNetConsumptionReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(
                new OperationNode(
                        Operator.PLUS,
                        new ReadingTypeRequirementNode(production),
                        new OperationNode(
                                Operator.MULTIPLY,
                                new ReadingTypeRequirementNode(consumption),
                                new ConstantNode(BigDecimal.valueOf(2L)))))
                .when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        when(consumption.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWhForward));
        when(consumption.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.consumption15MinChannel));
        when(production.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(thirtyMinuteskWhReverse));
        when(production.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.production30MinChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + CONSUMPTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + PRODUCTION_REQUIREMENT_ID + ".*" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(productionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + NET_CONSUMPTION_DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that the with clause for the the production requirement is aggregated to monthly values
            String productionWithSelectClause = this.productionWithClauseBuilder.getText();
            assertThat(productionWithSelectClause).matches(".*[sum|SUM]\\(value\\).*");
            assertThat(productionWithSelectClause).matches(".*[trunc|TRUNC]\\(localdate, 'MONTH'\\).*");
            assertThat(productionWithSelectClause).matches(".*[group by trunc|GROUP BY TRUNC]\\(localdate, 'MONTH'\\).*");
            // Assert that the with clause for the the consumption requirement is aggregated to monthly values
            String consumptionWithSelectClause = this.consumptionWithClauseBuilder.getText();
            assertThat(consumptionWithSelectClause).matches(".*[sum|SUM]\\(value\\).*");
            assertThat(consumptionWithSelectClause).matches(".*[trunc|TRUNC]\\(localdate, 'MONTH'\\).*");
            assertThat(consumptionWithSelectClause).matches(".*[group by trunc|GROUP BY TRUNC]\\(localdate, 'MONTH'\\).*");
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that one of both requirements' values are added up in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value \\+ \\(rid98_99_1\\.value\\s\\*\\s*\\?\\s*\\)\\).*");
            // Assert that the with clauses for both requirements are joined on the utc timestamp
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*JOIN rid98_99_1 ON rid98_99_1\\.timestamp = rid97_99_1\\.timestamp.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod99_1\\.value, rod99_1\\.timestamp.*");
        }
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private ReadingType mock15minReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getMRID()).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        return readingType;
    }

    private ReadingType mockMOnthlyNetConsumptionReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getMRID()).thenReturn(MONTHLY_NET_CONSUMPTION_MRID);
        return readingType;
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase).create();
    }

    private void setupUsagePoint(String mRID) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(mRID).create();
    }

    private void activateMeterWithAll15MinChannels() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2015);
        this.production15MinChannel = this.meterActivation.createChannel(fifteenMinuteskWhForward);
        this.consumption15MinChannel = this.meterActivation.createChannel(fifteenMinuteskWhReverse);
    }

    private void activateMeterWithAll15And30MinChannels() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2015);
        this.production30MinChannel = this.meterActivation.createChannel(thirtyMinuteskWhReverse);
        this.consumption15MinChannel = this.meterActivation.createChannel(fifteenMinuteskWhReverse);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}