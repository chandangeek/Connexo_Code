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
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.FormulaPart;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
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
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method
 * when temparature conversion (K, °C, °F) is required.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (10:55)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateWithTemperatureConversionIT {

    public static final String DAILY_TEMPERATURE_CELCIUS_MRID = "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0";
    public static final String DAILY_TEMPERATURE_FAHRENHEIT_MRID = "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.279.0";
    public static final String DAILY_TEMPERATURE_KELVIN_MRID = "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.6.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType K_15min;
    private static ReadingType C_15min;
    private static ReadingType F_15min;
    private static ReadingType K_daily;
    private static ReadingType C_daily;
    private static ReadingType F_daily;
    private static Instant jan1st2015 = Instant.ofEpochMilli(1420070400000L);
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static long TEMPERATURE1_REQUIREMENT_ID = 97L;
    private static long TEMPERATURE2_REQUIREMENT_ID = 98L;
    private static long DELIVERABLE_ID = 99L;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    @Mock
    private MetrologyConfiguration configuration;
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
                            "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.6.0",    // macro period: daily, averages, Kelvin
                            "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0",   // macro period: daily, averages, degrees celcius
                            "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.279.0",  // macro period: daily, averages, degrees Fahrenheit
                            "0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.6.0",     // no macro period, 15 min, Kelvin
                            "0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0",    // no macro period, 15 min, degrees Celcius
                            "0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.279.0"    // no macro period, 15 min, degrees Fahrenheit
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
            K_15min = getMeteringService().getReadingType("0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.6.0").get();
            C_15min = getMeteringService().getReadingType("0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0").get();
            F_15min = getMeteringService().getReadingType("0.0.2.0.0.7.46.0.0.0.0.0.0.0.0.0.279.0").get();
            K_daily = getMeteringService().getReadingType("11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.6.0").get();
            C_daily = getMeteringService().getReadingType("11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0").get();
            F_daily = getMeteringService().getReadingType("11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.279.0").get();
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
        this.temperatureWithClauseBuilder = new SqlBuilder();
        this.deliverableWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + TEMPERATURE1_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.temperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + DELIVERABLE_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(this.deliverableWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
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
     *       Jan 1st 2015 -> forever
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

        // Setup configuration requirements
        ReadingTypeRequirement temperature = mock(ReadingTypeRequirement.class);
        when(temperature.getName()).thenReturn("T");
        when(temperature.getId()).thenReturn(TEMPERATURE1_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Collections.singletonList(temperature));
        // Setup configuration deliverables
        ReadingTypeDeliverable avgTemperature = mock(ReadingTypeDeliverable.class);
        when(avgTemperature.getId()).thenReturn(DELIVERABLE_ID);
        when(avgTemperature.getName()).thenReturn("averageT");
        when(avgTemperature.getReadingType()).thenReturn(C_daily);
        FormulaBuilder formulaBuilder = newFormulaBuilder();
        FormulaPart node = formulaBuilder.plus(
                formulaBuilder.requirement(temperature),
                formulaBuilder.constant(BigDecimal.TEN)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).expressionNode();
        when(avgTemperature.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(avgTemperature));
        // Setup meter activations
        when(temperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(K_15min));
        when(temperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.temperatureChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE1_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value\\s*\\+\\s*\\?\\s*\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_CELCIUS_MRID) + "'.*");
            /* Assert that the overall select statement converts the Kelvin values to Celcius
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(rod99_1\\.value\\s*-\\s*273\\.15\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
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
     *       Jan 1st 2015 -> forever
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

        // Setup configuration requirements
        ReadingTypeRequirement temperature = mock(ReadingTypeRequirement.class);
        when(temperature.getName()).thenReturn("T");
        when(temperature.getId()).thenReturn(TEMPERATURE1_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Collections.singletonList(temperature));
        // Setup configuration deliverables
        ReadingTypeDeliverable avgTemperature = mock(ReadingTypeDeliverable.class);
        when(avgTemperature.getId()).thenReturn(DELIVERABLE_ID);
        when(avgTemperature.getName()).thenReturn("averageT");
        when(avgTemperature.getReadingType()).thenReturn(F_daily);
        FormulaBuilder formulaBuilder = newFormulaBuilder();
        FormulaPart node = formulaBuilder.plus(
                formulaBuilder.requirement(temperature),
                formulaBuilder.constant(BigDecimal.TEN)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).expressionNode();
        when(avgTemperature.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(avgTemperature));
        // Setup meter activations
        when(temperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(K_15min));
        when(temperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(this.temperatureChannel));

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE1_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(temperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that the formula is applied to the requirements' value in the select clause
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value\\s*\\+\\s*\\?\\s*\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_FAHRENHEIT_MRID) + "'.*");
            /* Assert that the overall select statement converts the Kelvin values to Celcius
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(\\(9\\s*\\*\\s*\\(rod99_1\\.value\\s*-\\s*255\\.3722*\\)\\)\\s*/\\s*5\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
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
     *       Jan 1st 2015 -> forever
     *           minT -> 15 min °C
     *           minT -> 15 min °F
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
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2015);
        Channel minTChannel = this.meterActivation.createChannel(C_15min);
        Channel maxTChannel = this.meterActivation.createChannel(F_15min);

        // Setup configuration requirements
        ReadingTypeRequirement minTemperature = mock(ReadingTypeRequirement.class);
        when(minTemperature.getName()).thenReturn("minT");
        when(minTemperature.getId()).thenReturn(TEMPERATURE1_REQUIREMENT_ID);
        ReadingTypeRequirement maxTemperature = mock(ReadingTypeRequirement.class);
        when(maxTemperature.getName()).thenReturn("maxT");
        when(maxTemperature.getId()).thenReturn(TEMPERATURE2_REQUIREMENT_ID);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(minTemperature, maxTemperature));
        // Setup configuration deliverables
        ReadingTypeDeliverable avgTemperature = mock(ReadingTypeDeliverable.class);
        when(avgTemperature.getId()).thenReturn(DELIVERABLE_ID);
        when(avgTemperature.getName()).thenReturn("averageT");
        when(avgTemperature.getReadingType()).thenReturn(K_daily);
        FormulaBuilder formulaBuilder = newFormulaBuilder();
        FormulaPart node =
                formulaBuilder.divide(
                    formulaBuilder.plus(
                        formulaBuilder.requirement(minTemperature),
                        formulaBuilder.requirement(maxTemperature)),
                    formulaBuilder.constant(BigDecimal.valueOf(2L))).create();

        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).expressionNode();
        when(avgTemperature.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(avgTemperature));
        // Setup meter activations
        when(minTemperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(C_15min));
        when(minTemperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(minTChannel));
        when(maxTemperature.getMatchesFor(this.meterActivation)).thenReturn(Collections.singletonList(F_15min));
        when(maxTemperature.getMatchingChannelsFor(this.meterActivation)).thenReturn(Collections.singletonList(maxTChannel));
        SqlBuilder minTemperatureWithClauseBuilder = new SqlBuilder();
        SqlBuilder maxTemperatureWithClauseBuilder = new SqlBuilder();
        when(clauseAwareSqlBuilder.with(matches("rid" + TEMPERATURE1_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(minTemperatureWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + TEMPERATURE2_REQUIREMENT_ID + ".*"), any(Optional.class), anyVararg())).thenReturn(maxTemperatureWithClauseBuilder);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE1_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(minTemperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rid" + TEMPERATURE2_REQUIREMENT_ID + ".*" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            assertThat(maxTemperatureWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + DELIVERABLE_ID + ".*1"),
                        any(Optional.class),
                        anyVararg());
            // Assert that one of the requirements is used as source for the timeline
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT -1, rid97_99_1\\.timestamp,.*");
            // Assert that the max temperature requirements' value is not coverted
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(rid97_99_1\\.value\\s*\\+\\s*\\(.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the max temperature requirements' value is coverted to Celcius
            assertThat(this.deliverableWithClauseBuilder.getText())
                    .matches("SELECT.*\\(255.3722*\\s*\\+\\s*\\(\\(5\\s*\\*\\s*rid98_99_1\\.value\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(DAILY_TEMPERATURE_KELVIN_MRID) + "'.*");
            /* Assert that the overall select statement converts the Celcius values to Kelvin
             * first and then takes the average to group by day. */
            assertThat(overallSelectWithoutNewlines).matches(".*[avg|AVG]\\(\\(273.15*\\s*\\+\\s*rod99_1\\.value\\)\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[trunc|TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
            assertThat(overallSelectWithoutNewlines).matches(".*[group by trunc|GROUP BY TRUNC]\\(rod99_1\\.localdate, 'DDD'\\).*");
        }
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

    private static MetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(MetrologyConfigurationService.class);
    }

    private static FormulaBuilder newFormulaBuilder() {
        return getMetrologyConfigurationService().newFormulaBuilder(Formula.Mode.AUTO);
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase).create();
    }

    private void setupUsagePoint(String mRID) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.GAS).get();
        this.usagePoint = electricity.newUsagePoint(mRID, jan1st2015).create();
    }

    private void activateMeterWithKelvin() {
        this.activateMeter(K_15min);
    }

    private void activateMeter(ReadingType readingType) {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2015);
        this.temperatureChannel = this.meterActivation.createChannel(readingType);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}