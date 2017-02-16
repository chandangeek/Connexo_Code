/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.MetrologyContractDoesNotApplyToUsagePointException;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.metering.impl.config.ServerFormulaBuilder;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private MeterRole meterRole;
    @Mock
    private MetrologyContract contract;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private QueryExecutor<EffectiveMetrologyConfigurationOnUsagePoint> queryExecutor;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private CustomPropertySetService customPropertySetService;
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
    private ReadingTypeDeliverableForMeterActivationFactory readingTypeDeliverableForMeterActivationFactory;
    @Mock
    private ClauseAwareSqlBuilder sqlBuilder;
    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MeteringDataModelService meteringDataModelService;
    @Mock
    private NlsService nlsService;

    private ServerMetrologyConfigurationService metrologyConfigurationService;
    private SqlBuilder withClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;

    @Before
    public void initializeMocks() throws SQLException {
        when(this.usagePoint.getName()).thenReturn("DataAggregationServiceImplCalculateTest");
        when(this.usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.effectiveMetrologyConfiguration));
        when(this.metrologyPurpose.getName()).thenReturn("DataAggregationServiceImplCalculateTest");
        when(this.contract.getMetrologyPurpose()).thenReturn(this.metrologyPurpose);
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
        when(this.dataModel.getInstance(CalculatedReadingRecordFactory.class)).thenReturn(new CalculatedReadingRecordFactoryImpl(this.dataModel, meteringService));
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringDataModelService, this.dataModel, this.thesaurus);
        when(this.configuration.getContracts()).thenReturn(Collections.singletonList(this.contract));
        when(this.dataModel.query(eq(EffectiveMetrologyConfigurationOnUsagePoint.class), anyVararg())).thenReturn(this.queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(this.effectiveMetrologyConfiguration));
        when(this.effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(this.configuration);
        when(this.effectiveMetrologyConfiguration.getRange()).thenReturn(year2016());
        when(this.effectiveMetrologyConfiguration.getInterval()).thenReturn(Interval.of(year2016()));
        when(this.usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
    }

    /**
     * Tests the case where data aggregation is requested for a
     * {@link MetrologyContract} that is not active on the {@link UsagePoint}
     * because no {@link MetrologyConfiguration} has been applied
     * to the UsagePoint yet.
     */
    @Test(expected = MetrologyContractDoesNotApplyToUsagePointException.class)
    public void noMetrologyConfigurationsAppliedToUsagePoint() {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.emptyList());

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        //Asserts: see expected exception rule
    }

    /**
     * Tests the case where data aggregation is requested for a
     * {@link MetrologyContract} that is not active on the {@link UsagePoint}
     * because no {@link MetrologyConfiguration} has been applied
     * to the UsagePoint yet.
     */
    @Test(expected = MetrologyContractDoesNotApplyToUsagePointException.class)
    public void otherMetrologyConfigurationAppliedToUsagePoint() {
        MetrologyContract otherContract = mock(MetrologyContract.class);
        UsagePointMetrologyConfiguration otherConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(otherConfiguration.getContracts()).thenReturn(Collections.singletonList(otherContract));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(otherConfiguration);
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(effectiveMetrologyConfiguration));

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        //Asserts: see expected exception rule
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
    public void simplestNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getDimension()).thenReturn(Dimension.ENERGY);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));
        when(this.configuration.getMeterRoleFor(production)).thenReturn(Optional.of(this.meterRole));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType("0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.plus(
                formulaBuilder.requirement(production),
                formulaBuilder.requirement(consumption)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).getExpressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumption = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumption.sqlName()).thenReturn("vrt-consumption");
        VirtualReadingTypeRequirement virtualProduction = mock(VirtualReadingTypeRequirement.class);
        when(virtualProduction.sqlName()).thenReturn("vrt-production");
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualProduction);

        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));

        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(this.meterRole));
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
        when(consumption.getMatchesFor(channelsContainer)).thenReturn(Collections.singletonList(productionReadingType15min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn2));
        when(this.virtualFactory.allRequirements()).thenReturn(Arrays.asList(virtualConsumption, virtualProduction));
        ReadingTypeDeliverableForMeterActivationSet netConsumptionForMeterActivation = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(netConsumptionForMeterActivation.sqlName()).thenReturn("vrt-netConsumption");
        when(this.readingTypeDeliverableForMeterActivationFactory
                .from(
                        eq(Formula.Mode.AUTO),
                        eq(netConsumption),
                        any(MeterActivationSet.class),
                        eq(aggregationPeriod),
                        anyInt(),
                        any(ServerExpressionNode.class),
                        any(VirtualReadingType.class)))
                .thenReturn(netConsumptionForMeterActivation);

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        verify(this.virtualFactory).nextMeterActivationSet(any(MeterActivationSet.class), eq(aggregationPeriod));
        ArgumentCaptor<VirtualReadingType> consumptionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), consumptionReadingTypeArgumentCaptor.capture());
        VirtualReadingType capturedConsumptionReadingType = consumptionReadingTypeArgumentCaptor.getValue();
        assertThat(capturedConsumptionReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        ArgumentCaptor<VirtualReadingType> productionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), productionReadingTypeArgumentCaptor.capture());
        VirtualReadingType capturedProductionReadngType = productionReadingTypeArgumentCaptor.getValue();
        assertThat(capturedProductionReadngType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        verify(this.virtualFactory).allRequirements();
        verify(netConsumptionForMeterActivation).appendDefinitionTo(this.sqlBuilder);
        verify(virtualConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProduction).appendDefinitionTo(this.sqlBuilder);
        verify(netConsumptionForMeterActivation).appendDefinitionTo(this.sqlBuilder);
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
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * A+ ::= any Wh with flow = reverse (aka production)
     * deliverables:
     * netConsumption (monthly kWh) ::= A- + A+
     * Device:
     * meter activations:
     * Jan 1st 2015 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     *
     * @see #simplestNetConsumptionOfProsumer()
     */
    @Test
    public void monthlyNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getDimension()).thenReturn(Dimension.ENERGY);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));
        when(this.configuration.getMeterRoleFor(production)).thenReturn(Optional.of(this.meterRole));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType("13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.plus(
                formulaBuilder.requirement(production),
                formulaBuilder.requirement(consumption)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).getExpressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumption = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumption.sqlName()).thenReturn("vrt-consumption");
        VirtualReadingTypeRequirement virtualProduction = mock(VirtualReadingTypeRequirement.class);
        when(virtualProduction.sqlName()).thenReturn("vrt-production");
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumption);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualProduction);

        // Setup contract deliverables

        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));

        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(this.meterRole));
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
        when(consumption.getMatchesFor(channelsContainer)).thenReturn(Collections.singletonList(productionReadingType15min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn2));
        when(this.virtualFactory.allRequirements()).thenReturn(Arrays.asList(virtualConsumption, virtualProduction));
        ReadingTypeDeliverableForMeterActivationSet netConsumptionForMeterActivation = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(netConsumptionForMeterActivation.sqlName()).thenReturn("vrt-netConsumption");
        when(this.readingTypeDeliverableForMeterActivationFactory
                .from(
                        eq(Formula.Mode.AUTO),
                        eq(netConsumption),
                        any(MeterActivationSet.class),
                        eq(aggregationPeriod),
                        anyInt(),
                        any(ServerExpressionNode.class),
                        any(VirtualReadingType.class)))
                .thenReturn(netConsumptionForMeterActivation);

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        verify(this.virtualFactory).nextMeterActivationSet(any(MeterActivationSet.class), eq(aggregationPeriod));
        ArgumentCaptor<VirtualReadingType> consumptionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), consumptionReadingTypeArgumentCaptor.capture());
        VirtualReadingType capturedConsumptionReadingType = consumptionReadingTypeArgumentCaptor.getValue();
        assertThat(capturedConsumptionReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        ArgumentCaptor<VirtualReadingType> productionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory).requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), productionReadingTypeArgumentCaptor.capture());
        VirtualReadingType capturedProductionReadngType = productionReadingTypeArgumentCaptor.getValue();
        assertThat(capturedProductionReadngType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        verify(this.virtualFactory).allRequirements();
        verify(virtualConsumption).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProduction).appendDefinitionTo(this.sqlBuilder);
        verify(netConsumptionForMeterActivation).appendDefinitionTo(this.sqlBuilder);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
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
     * Jan 1st 2015 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * Feb 1st 2015 -> forever
     * A- -> 15 min kWh
     * A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel for all meter activations.
     */
    @Test
    public void simplestNetConsumptionOfProsumerWithMultipleMeterActivations() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(production.getDimension()).thenReturn(Dimension.ENERGY);
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));
        when(this.configuration.getMeterRoleFor(production)).thenReturn(Optional.of(this.meterRole));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType("0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.plus(
                formulaBuilder.requirement(production),
                formulaBuilder.requirement(consumption)).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).getExpressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumptionJan = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumptionJan.sqlName()).thenReturn("vrt-consumption-jan");
        VirtualReadingTypeRequirement virtualConsumptionFeb = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumptionFeb.sqlName()).thenReturn("vrt-consumption-feb");
        VirtualReadingTypeRequirement virtualProductionJan = mock(VirtualReadingTypeRequirement.class);
        when(virtualProductionJan.sqlName()).thenReturn("vrt-production-jan");
        VirtualReadingTypeRequirement virtualProductionFeb = mock(VirtualReadingTypeRequirement.class);
        when(virtualProductionFeb.sqlName()).thenReturn("vrt-production-feb");
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualConsumptionJan, virtualConsumptionFeb);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), any(VirtualReadingType.class))).thenReturn(virtualProductionJan, virtualProductionFeb);

        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));

        // Setup meter activations
        MeterActivation meterActivation1 = mock(MeterActivation.class);
        ChannelsContainer channelsContainer1 = mock(ChannelsContainer.class);
        when(meterActivation1.getChannelsContainer()).thenReturn(channelsContainer1);
        when(meterActivation1.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation1.getMeterRole()).thenReturn(Optional.of(this.meterRole));
        when(meterActivation1.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        when(meterActivation1.getInterval()).thenReturn(Interval.of(jan1st2015(), feb1st2015()));
        when(meterActivation1.getRange()).thenReturn(Interval.of(jan1st2015(), feb1st2015()).toClosedOpenRange());
        when(meterActivation1.overlaps(aggregationPeriod)).thenReturn(true);
        MeterActivation meterActivation2 = mock(MeterActivation.class);
        ChannelsContainer channelsContainer2 = mock(ChannelsContainer.class);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(meterActivation2.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation2.getMeterRole()).thenReturn(Optional.of(this.meterRole));
        when(meterActivation2.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        when(meterActivation2.getInterval()).thenReturn(Interval.startAt(feb1st2015()));
        when(meterActivation2.getRange()).thenReturn(Interval.startAt(feb1st2015()).toClosedOpenRange());
        when(meterActivation2.overlaps(aggregationPeriod)).thenReturn(true);
        doReturn(Arrays.asList(meterActivation1, meterActivation2)).when(this.usagePoint).getMeterActivations();
        ReadingType consumptionReadingType15min = this.mock15minReadingType("0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ReadingType productionReadingType15min = this.mock15minReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ChannelContract chnJan1 = mock(ChannelContract.class);
        when(chnJan1.getMainReadingType()).thenReturn(consumptionReadingType15min);
        when(virtualConsumptionJan.getPreferredChannel()).thenReturn(chnJan1);
        ChannelContract chnJan2 = mock(ChannelContract.class);
        when(chnJan2.getMainReadingType()).thenReturn(productionReadingType15min);
        when(virtualProductionJan.getPreferredChannel()).thenReturn(chnJan2);
        ChannelContract chnFeb1 = mock(ChannelContract.class);
        when(chnFeb1.getMainReadingType()).thenReturn(consumptionReadingType15min);
        when(virtualConsumptionFeb.getPreferredChannel()).thenReturn(chnFeb1);
        ChannelContract chnFeb2 = mock(ChannelContract.class);
        when(chnFeb2.getMainReadingType()).thenReturn(productionReadingType15min);
        when(virtualProductionFeb.getPreferredChannel()).thenReturn(chnFeb2);
        when(consumption.getMatchingChannelsFor(channelsContainer1)).thenReturn(Collections.singletonList(chnJan1));
        when(production.getMatchingChannelsFor(channelsContainer1)).thenReturn(Collections.singletonList(chnJan2));
        when(consumption.getMatchingChannelsFor(channelsContainer2)).thenReturn(Collections.singletonList(chnJan1));
        when(production.getMatchingChannelsFor(channelsContainer2)).thenReturn(Collections.singletonList(chnJan2));
        when(this.virtualFactory.allRequirements()).thenReturn(Arrays.asList(virtualConsumptionJan, virtualProductionJan, virtualConsumptionFeb, virtualProductionFeb));

        ReadingTypeDeliverableForMeterActivationSet netConsumptionForJan = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(netConsumptionForJan.sqlName()).thenReturn("vrt-netConsumption-jan");
        ReadingTypeDeliverableForMeterActivationSet netConsumptionForFeb = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        when(netConsumptionForJan.sqlName()).thenReturn("vrt-netConsumption-feb");
        when(this.readingTypeDeliverableForMeterActivationFactory
                .from(
                        eq(Formula.Mode.AUTO),
                        eq(netConsumption),
                        any(MeterActivationSet.class),
                        eq(aggregationPeriod),
                        anyInt(),
                        any(ServerExpressionNode.class),
                        any(VirtualReadingType.class)))
                .thenReturn(
                        netConsumptionForJan,
                        netConsumptionForFeb);

        // Business method
        service.calculate(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        verify(this.virtualFactory, times(2)).nextMeterActivationSet(any(MeterActivationSet.class), eq(aggregationPeriod));
        ArgumentCaptor<VirtualReadingType> consumptionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory, times(2)).requirementFor(eq(Formula.Mode.AUTO), eq(consumption), eq(netConsumption), consumptionReadingTypeArgumentCaptor.capture());
        List<VirtualReadingType> capturedConsumptionReadingTypes = consumptionReadingTypeArgumentCaptor.getAllValues();
        assertThat(capturedConsumptionReadingTypes).hasSize(2);
        assertThat(capturedConsumptionReadingTypes.get(0).getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(capturedConsumptionReadingTypes.get(1).getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        ArgumentCaptor<VirtualReadingType> productionReadingTypeArgumentCaptor = ArgumentCaptor.forClass(VirtualReadingType.class);
        verify(this.virtualFactory, times(2)).requirementFor(eq(Formula.Mode.AUTO), eq(production), eq(netConsumption), productionReadingTypeArgumentCaptor.capture());
        List<VirtualReadingType> capturedProductionReadingTypes = productionReadingTypeArgumentCaptor.getAllValues();
        assertThat(capturedProductionReadingTypes).hasSize(2);
        assertThat(capturedProductionReadingTypes.get(0).getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        assertThat(capturedProductionReadingTypes.get(1).getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
        verify(this.virtualFactory).allRequirements();
        verify(virtualConsumptionJan).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProductionJan).appendDefinitionTo(this.sqlBuilder);
        verify(netConsumptionForJan).appendDefinitionTo(this.sqlBuilder);
        verify(virtualConsumptionFeb).appendDefinitionTo(this.sqlBuilder);
        verify(virtualProductionFeb).appendDefinitionTo(this.sqlBuilder);
        verify(netConsumptionForFeb).appendDefinitionTo(this.sqlBuilder);
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Instant feb1st2015() {
        return Instant.ofEpochMilli(1454284800000L);
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
        return new DataAggregationServiceImpl(
                this.customPropertySetService,
                this.meteringService,
                new InstantTruncaterFactory(this.meteringService),
                this::getSqlBuilderFactory,
                this::getVirtualFactory,
                this::getReadingTypeDeliverableForMeterActivationFactory);
    }

    private SqlBuilderFactory getSqlBuilderFactory() {
        return sqlBuilderFactory;
    }

    private ReadingTypeDeliverableForMeterActivationFactory getReadingTypeDeliverableForMeterActivationFactory() {
        return readingTypeDeliverableForMeterActivationFactory;
    }

    private VirtualFactory getVirtualFactory() {
        return this.virtualFactory;
    }

    private ServerFormulaBuilder newFormulaBuilder() {
        return this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
    }

}