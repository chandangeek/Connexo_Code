package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
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
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
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
import static org.mockito.Mockito.doReturn;
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

    public static final String CELCIUS_15_MIN_MRID = "0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0";
    public static final String MILLIBAR_15_MIN_MRID = "0.0.2.0.0.7.0.0.0.0.0.0.0.0.0.0.214.0";
    public static final String MEGA_JOULE_15_MIN_MRID = "0.0.2.0.0.7.12.0.0.0.0.0.0.0.0.6.31.0";
    public static final String MEGA_JOULE_DAILY_MRID = "11.2.2.0.0.7.12.0.0.0.0.0.0.0.0.6.31.0";
    public static final String CUBIC_METER_15_MIN_MRID = "0.0.2.0.0.7.58.0.0.0.0.0.0.0.0.0.42.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType CELCIUS_15min;
    private static ReadingType PRESSURE_15min;
    private static ReadingType VOLUME_15min;
    private static ReadingType ENERGY_15min;
    private static ReadingType ENERGY_daily;
    private static Instant jan1st2015 = Instant.ofEpochMilli(1420070400000L);
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static long TEMPERATURE_REQUIREMENT_ID = 97L;
    private static long PRESSURE_REQUIREMENT_ID = 98L;
    private static long VOLUME_REQUIREMENT_ID = 99L;
    private static long DELIVERABLE_ID = 100L;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    @Mock
    private MetrologyConfiguration configuration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
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
                            MEGA_JOULE_15_MIN_MRID,
                            MEGA_JOULE_DAILY_MRID,
                            MILLIBAR_15_MIN_MRID,
                            CELCIUS_15_MIN_MRID,
                            CUBIC_METER_15_MIN_MRID
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
            CELCIUS_15min = getMeteringService().getReadingType(CELCIUS_15_MIN_MRID).get();
            PRESSURE_15min = getMeteringService().getReadingType(MILLIBAR_15_MIN_MRID).get();
            VOLUME_15min = getMeteringService().getReadingType(CUBIC_METER_15_MIN_MRID).get();
            ENERGY_15min = getMeteringService().getReadingType(MEGA_JOULE_15_MIN_MRID).get();
            ENERGY_daily = getMeteringService().getReadingType(MEGA_JOULE_DAILY_MRID).get();
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        when(this.usagePoint.getName()).thenReturn("DataAggregationServiceImplExpertModeIT");
        when(this.metrologyPurpose.getName()).thenReturn("DataAggregationServiceImplExpertModeIT");
        when(this.contract.getMetrologyPurpose()).thenReturn(this.metrologyPurpose);
        this.temperatureWithClauseBuilder = new SqlBuilder();
        this.pressureWithClauseBuilder = new SqlBuilder();
        this.volumeWithClauseBuilder = new SqlBuilder();
        this.deliverableWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + TEMPERATURE_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.temperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + PRESSURE_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.pressureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + VOLUME_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.volumeWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + DELIVERABLE_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.deliverableWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
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
     *    requirements:
     *       T ::= Celcius  (15m)
     *       P ::= millibar (15m)
     *       V ::= m3       (15m)
     *    deliverables:
     *       Energy (15min °C) ::= V * (T / P * 1013.25 / 288.15)
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           T -> 15 min °C
     *           P -> 15 min millibar
     *           V -> 15 min m3
     * In other words, all requirements are provided by exactly
     * one matching channel from a single meter activation.
     */
    @Test
    @Transactional
    public void energyFromGasVolume() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("energyFromGasVolume");
        this.setupUsagePoint("energyFromGasVolume");
        this.activateMeterWithCelcius();

        // Setup configuration requirements
        ReadingTypeRequirement temperature = mock(ReadingTypeRequirement.class);
        when(temperature.getName()).thenReturn("T");
        when(temperature.getId()).thenReturn(TEMPERATURE_REQUIREMENT_ID);
        ReadingTypeRequirement pressure = mock(ReadingTypeRequirement.class);
        when(pressure.getName()).thenReturn("T");
        when(pressure.getId()).thenReturn(PRESSURE_REQUIREMENT_ID);
        ReadingTypeRequirement volume = mock(ReadingTypeRequirement.class);
        when(volume.getName()).thenReturn("T");
        when(volume.getId()).thenReturn(VOLUME_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(temperature, pressure, volume));
        // Setup configuration deliverables
        ReadingTypeDeliverable energy = mock(ReadingTypeDeliverable.class);
        when(energy.getId()).thenReturn(DELIVERABLE_ID);
        when(energy.getName()).thenReturn("Energy");
        when(energy.getReadingType()).thenReturn(ENERGY_15min);
        FormulaBuilder formulaBuilder = newFormulaBuilder();
        ExpressionNode node =
                formulaBuilder.multiply(
                    formulaBuilder.constant(BigDecimal.valueOf(40L)),   // calorific value
                    formulaBuilder.multiply(
                        formulaBuilder.requirement(volume),
                        formulaBuilder.multiply(
                                formulaBuilder.divide(
                                        formulaBuilder.requirement(temperature),
                                        formulaBuilder.requirement(pressure)),
                                formulaBuilder.divide(
                                        formulaBuilder.constant(BigDecimal.valueOf(101325L, 2)),    // 1013,25 normal pressure at sea level
                                        formulaBuilder.constant(BigDecimal.valueOf(15L))))))        // 15 normalized gas is measured at 15 °Celcius
                    .create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.EXPERT);
        doReturn(node).when(formula).getExpressionNode();
        when(energy.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(energy));
        // Setup meter activations
        when(temperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(CELCIUS_15min));
        when(temperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.temperatureChannel));
        when(pressure.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(PRESSURE_15min));
        when(pressure.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.pressureChannel));
        when(volume.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(VOLUME_15min));
        when(volume.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.volumeChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + PRESSURE_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(pressureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + VOLUME_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(volumeWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            String deliverableWithClauseSql = this.deliverableWithClauseBuilder.getText().replace("\n", " ");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT -1, rid99_100_1\\.timestamp,.*rid99_100_1\\.processStatus,.*rid99_100_1\\.localdate\\s*FROM.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*\\(\\s*\\?\\s*\\* \\(rid99_100_1\\.value \\* \\(\\(rid97_100_1\\.value / rid98_100_1\\.value\\) \\* \\(\\s*\\?\\s*/\\s*\\?\\s*\\)\\)\\)\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MEGA_JOULE_15_MIN_MRID) + "'.*");
            /* Assert that the overall select statement selects the value from the deliverable. */
            assertThat(overallSelectWithoutNewlines).matches(".*rod100_1\\.value.*");
        }
    }

    /**
     * Tests the expert mode formula: Energy = Normalized volume * calorific value
     * where Normalized volume is calculated as: volume * (temperature / pressure * normalized-temperature / normalized-pressure).
     * Both normalized-temperature and normalized-pressure are in fact constants.
     * Metrology configuration
     *    requirements:
     *       T ::= Celcius  (15m)
     *       P ::= millibar (15m)
     *       V ::= m3       (15m)
     *    deliverables:
     *       Energy (daily °C) ::= V * (T / P * 1013.25 / 288.15)
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           T -> 15 min °C
     *           P -> 15 min millibar
     *           V -> 15 min m3
     * In other words, all requirements are provided by exactly
     * one matching channel from a single meter activation.
     */
    @Test
    @Transactional
    public void energyFromGasVolumeWithDailyAggregation() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("energyFromGasVolume");
        this.setupUsagePoint("energyFromGasVolume");
        this.activateMeterWithCelcius();

        // Setup configuration requirements
        ReadingTypeRequirement temperature = mock(ReadingTypeRequirement.class);
        when(temperature.getName()).thenReturn("T");
        when(temperature.getId()).thenReturn(TEMPERATURE_REQUIREMENT_ID);
        ReadingTypeRequirement pressure = mock(ReadingTypeRequirement.class);
        when(pressure.getName()).thenReturn("T");
        when(pressure.getId()).thenReturn(PRESSURE_REQUIREMENT_ID);
        ReadingTypeRequirement volume = mock(ReadingTypeRequirement.class);
        when(volume.getName()).thenReturn("T");
        when(volume.getId()).thenReturn(VOLUME_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(temperature, pressure, volume));
        // Setup configuration deliverables
        ReadingTypeDeliverable energy = mock(ReadingTypeDeliverable.class);
        when(energy.getId()).thenReturn(DELIVERABLE_ID);
        when(energy.getName()).thenReturn("Energy");
        when(energy.getReadingType()).thenReturn(ENERGY_daily);
        FormulaBuilder formulaBuilder = newFormulaBuilder();
        ExpressionNode node =
                formulaBuilder.aggregate(   // Note how the expert is required to define when the aggregation is done
                    formulaBuilder.multiply(
                        formulaBuilder.constant(BigDecimal.valueOf(40L)),   // calorific value
                        formulaBuilder.multiply(
                            formulaBuilder.requirement(volume),
                            formulaBuilder.multiply(
                                    formulaBuilder.divide(
                                            formulaBuilder.requirement(temperature),
                                            formulaBuilder.requirement(pressure)),
                                    formulaBuilder.divide(
                                            formulaBuilder.constant(BigDecimal.valueOf(101325L, 2)),    // 1013,25 normal pressure at sea level
                                            formulaBuilder.constant(BigDecimal.valueOf(15L)))))))       // 15 normalized gas is measured at 15 °Celcius
                    .create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.EXPERT);
        doReturn(node).when(formula).getExpressionNode();
        when(energy.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(energy));
        // Setup meter activations
        when(temperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(CELCIUS_15min));
        when(temperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.temperatureChannel));
        when(pressure.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(PRESSURE_15min));
        when(pressure.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.pressureChannel));
        when(volume.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(VOLUME_15min));
        when(volume.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.volumeChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + PRESSURE_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(pressureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + VOLUME_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(volumeWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            String deliverableWithClauseSql = this.deliverableWithClauseBuilder.getText().replace("\n", " ");
            assertThat(deliverableWithClauseSql).startsWith("SELECT -1,");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[max|MAX]\\(rid99_100_1\\.timestamp\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*aggFlags\\(.*rid99_100_1\\.processStatus.*\\).*FROM.*");
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[trunc|TRUNC]\\(rid99_100_1\\.localdate, 'DDD'\\)\\s*FROM.*");
            // Assert that the formula and the aggregation function is applied to the requirements' value in the select clause
            assertThat(deliverableWithClauseSql)
                    .matches("SELECT.*[sum|SUM]\\(\\(\\s*\\?\\s*\\*\\s*\\(rid99_100_1\\.value \\* \\(\\(rid97_100_1\\.value / rid98_100_1\\.value\\) \\* \\(\\s*\\?\\s*/\\s*\\?\\s*\\)\\)\\)\\)\\).*FROM.*");
            assertThat(deliverableWithClauseSql).matches(".*[group by trunc|GROUP BY TRUNC]\\(rid99_100_1\\.localdate.*, 'DDD'\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MEGA_JOULE_DAILY_MRID) + "'.*");
            /* Assert that the overall select statement selects the value from the deliverable. */
            assertThat(overallSelectWithoutNewlines).matches(".*rod100_1\\.value.*");
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

    private static FormulaBuilder newFormulaBuilder() {
        return getMetrologyConfigurationService().newFormulaBuilder(Formula.Mode.EXPERT);
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase).create();
    }

    private void setupUsagePoint(String mRID) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        this.usagePoint = electricity.newUsagePoint(mRID, jan1st2015).create();
    }

    private void activateMeterWithCelcius() {
        this.activateMeter(CELCIUS_15min);
    }

    private void activateMeter(ReadingType readingType) {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2015);
        this.temperatureChannel = this.meterActivation.createChannel(readingType);
        this.pressureChannel = this.meterActivation.createChannel(PRESSURE_15min);
        this.volumeChannel = this.meterActivation.createChannel(VOLUME_15min);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}