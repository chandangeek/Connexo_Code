package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (15:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MetrologyConfiguration configuration;
    @Mock
    private MetrologyContract contract;
    @Mock
    private DataModel dataModel;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private SqlBuilderFactory sqlBuilderFactory;
    @Mock
    private ClauseAwareSqlBuilder sqlBuilder;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;

    private MetrologyConfigurationService metrologyConfigurationService;
    private SqlBuilder withClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;

    @Before
    public void initializeMocks() throws SQLException {
        this.withClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
        when(this.sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(this.sqlBuilder);
        when(this.sqlBuilder.with(anyString(), any(Optional.class), anyVararg())).thenReturn(this.withClauseBuilder);
        when(this.sqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(this.sqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
        when(this.meteringService.getDataModel()).thenReturn(this.dataModel);
        when(this.dataModel.getConnection(true)).thenReturn(this.connection);
        when(this.connection.prepareStatement(anyString())).thenReturn(this.preparedStatement);
        when(this.preparedStatement.executeQuery()).thenReturn(this.resultSet);
        when(this.dataModel.getInstance(AggregatedReadingRecordFactory.class)).thenReturn(new AggregatedReadingRecordFactoryImpl(this.dataModel));
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringService, this.eventService, this.userService);
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
    public void simplestNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType("0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        FormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.plus(
                formulaBuilder.requirement(production),
                formulaBuilder.requirement(consumption)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumption = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumption.sqlName()).thenReturn("vrt-consumption");
        VirtualReadingTypeRequirement virtualProduction = mock(VirtualReadingTypeRequirement.class);
        when(virtualProduction.sqlName()).thenReturn("vrt-production");
        when(this.virtualFactory.requirementFor(eq(consumption), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(this.virtualFactory.requirementFor(eq(production), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualProduction);

        // Setup contract deliverables
        VirtualReadingTypeDeliverable virtualNetConsumption = mock(VirtualReadingTypeDeliverable.class);
        when(virtualNetConsumption.sqlName()).thenReturn("vrt-netConsumption");
        when(this.virtualFactory.deliverableFor(any(ReadingTypeDeliverableForMeterActivation.class), any(VirtualReadingType.class))).thenReturn(virtualNetConsumption);
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        Interval year2015 = Interval.startAt(jan1st2015());
        when(meterActivation.getInterval()).thenReturn(year2015);
        when(meterActivation.getRange()).thenReturn(year2015.toClosedOpenRange());
        when(meterActivation.overlaps(aggregationPeriod)).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        ReadingType consumptionReadingType15min = this.mock15minReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ChannelContract chn1 = mock(ChannelContract.class);
        when(chn1.getMainReadingType()).thenReturn(consumptionReadingType15min);
        when(virtualConsumption.getPreferredChannel()).thenReturn(chn1);
        ReadingType productionReadingType15min = this.mock15minReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ChannelContract chn2 = mock(ChannelContract.class);
        when(chn2.getMainReadingType()).thenReturn(productionReadingType15min);
        when(virtualProduction.getPreferredChannel()).thenReturn(chn2);
        when(consumption.getMatchesFor(meterActivation)).thenReturn(Collections.singletonList(productionReadingType15min));
        when(consumption.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn2));
        when(this.virtualFactory.allRequirements()).thenReturn(Arrays.asList(virtualConsumption, virtualProduction));
        when(this.virtualFactory.allDeliverables()).thenReturn(Collections.singletonList(virtualNetConsumption));

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        verify(this.virtualFactory).nextMeterActivation(meterActivation, aggregationPeriod);
        ArgumentCaptor<VirtualReadingType> readingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(consumption), eq(netConsumption), readingTypeArgumentCaptor.capture());
        VirtualReadingType capturedConsumptionReadingType = readingTypeArgumentCaptor.getValue();
        assertThat(capturedConsumptionReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        verify(this.virtualFactory).requirementFor(eq(production), eq(netConsumption), readingTypeArgumentCaptor.capture());
        VirtualReadingType capturedProductionReadngType = readingTypeArgumentCaptor.getValue();
        assertThat(capturedProductionReadngType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        // Formula does not contain a reference to the deliverable
        verify(this.virtualFactory, never()).deliverableFor(any(ReadingTypeDeliverableForMeterActivation.class), any(VirtualReadingType.class));
        verify(this.virtualFactory).allRequirements();
        verify(this.virtualFactory).allDeliverables();
        verify(virtualConsumption, atLeastOnce()).sqlName();
        verify(virtualConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProduction, atLeastOnce()).sqlName();
        verify(virtualProduction).appendDefinitionTo(this.sqlBuilder);
        verify(virtualNetConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    /**
     * Simular to the simplest case above but the requirement
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
    public void monthlyNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType("13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        FormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.plus(
                formulaBuilder.requirement(production),
                formulaBuilder.requirement(consumption)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumption = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumption.sqlName()).thenReturn("vrt-consumption");
        VirtualReadingTypeRequirement virtualProduction = mock(VirtualReadingTypeRequirement.class);
        when(virtualProduction.sqlName()).thenReturn("vrt-production");
        when(this.virtualFactory.requirementFor(eq(consumption), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(this.virtualFactory.requirementFor(eq(production), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualProduction);

        // Setup contract deliverables
        VirtualReadingTypeDeliverable virtualNetConsumption = mock(VirtualReadingTypeDeliverable.class);
        when(virtualNetConsumption.sqlName()).thenReturn("vrt-netConsumption");
        when(this.virtualFactory.deliverableFor(any(ReadingTypeDeliverableForMeterActivation.class), any(VirtualReadingType.class))).thenReturn(virtualNetConsumption);
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        Interval year2015 = Interval.startAt(jan1st2015());
        when(meterActivation.getInterval()).thenReturn(year2015);
        when(meterActivation.getRange()).thenReturn(year2015.toClosedOpenRange());
        when(meterActivation.overlaps(aggregationPeriod)).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        ReadingType consumptionReadingType15min = this.mock15minReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ChannelContract chn1 = mock(ChannelContract.class);
        when(chn1.getMainReadingType()).thenReturn(consumptionReadingType15min);
        ReadingType productionReadingType15min = this.mock15minReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(virtualConsumption.getPreferredChannel()).thenReturn(chn1);
        ChannelContract chn2 = mock(ChannelContract.class);
        when(chn2.getMainReadingType()).thenReturn(productionReadingType15min);
        when(virtualProduction.getPreferredChannel()).thenReturn(chn2);
        when(consumption.getMatchesFor(meterActivation)).thenReturn(Collections.singletonList(productionReadingType15min));
        when(consumption.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn2));
        when(this.virtualFactory.allRequirements()).thenReturn(Arrays.asList(virtualConsumption, virtualProduction));
        when(this.virtualFactory.allDeliverables()).thenReturn(Collections.singletonList(virtualNetConsumption));

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        verify(this.virtualFactory).nextMeterActivation(meterActivation, aggregationPeriod);
        ArgumentCaptor<VirtualReadingType> readingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(consumption), eq(netConsumption), readingTypeArgumentCaptor.capture());
        VirtualReadingType capturedConsumptionReadingType = readingTypeArgumentCaptor.getValue();
        assertThat(capturedConsumptionReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        verify(this.virtualFactory).requirementFor(eq(production), eq(netConsumption), readingTypeArgumentCaptor.capture());
        VirtualReadingType capturedProductionReadngType = readingTypeArgumentCaptor.getValue();
        assertThat(capturedProductionReadngType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        // Formula does not contain a reference to the deliverable
        verify(this.virtualFactory, never()).deliverableFor(any(ReadingTypeDeliverableForMeterActivation.class), any(VirtualReadingType.class));
        verify(this.virtualFactory).allRequirements();
        verify(this.virtualFactory).allDeliverables();
        verify(virtualConsumption, atLeastOnce()).sqlName();
        verify(virtualConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProduction, atLeastOnce()).sqlName();
        verify(virtualProduction).appendDefinitionTo(this.sqlBuilder);
        verify(virtualNetConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Range<Instant> year2016() {
        return Range.atLeast(Instant.ofEpochMilli(1451606400000L));
    }

    private ReadingType mock15minReadingType(String mRID) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        return readingType;
    }

    private DataAggregationServiceImpl testInstance() {
        return new DataAggregationServiceImpl(this.meteringService, this::getVirtualFactory, this.sqlBuilderFactory);
    }

    private VirtualFactory getVirtualFactory() {
        return this.virtualFactory;
    }

    private FormulaBuilder newFormulaBuilder() {
        return this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
    }

}