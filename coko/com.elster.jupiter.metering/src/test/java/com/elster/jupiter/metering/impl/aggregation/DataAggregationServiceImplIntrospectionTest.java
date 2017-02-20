/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
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
import com.elster.jupiter.metering.impl.ServerUsagePoint;
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
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataAggregationServiceImpl#introspect(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-01 (15:37)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplIntrospectionTest {

    @Mock
    private ServerUsagePoint usagePoint;
    @Mock
    private UsagePointMetrologyConfiguration configuration;
    @Mock
    private ServiceCategory serviceCategory;
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
    private CalendarService calendarService;
    @Mock
    private ServerMeteringService meteringService;
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

    @Before
    public void initializeMocks() {
        when(this.usagePoint.getName()).thenReturn("DataAggregationServiceImplCalculateTest");
        when(this.usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(this.effectiveMetrologyConfiguration));
        when(this.metrologyPurpose.getName()).thenReturn("DataAggregationServiceImplCalculateTest");
        when(this.contract.getMetrologyPurpose()).thenReturn(this.metrologyPurpose);
        when(this.meteringService.getDataModel()).thenReturn(this.dataModel);
        when(this.dataModel.getInstance(CalculatedReadingRecordFactory.class)).thenReturn(new CalculatedReadingRecordFactoryImpl(this.dataModel, meteringService));
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this.meteringDataModelService, this.dataModel, this.thesaurus);
        when(this.configuration.getContracts()).thenReturn(Collections.singletonList(this.contract));
        when(this.dataModel.query(eq(EffectiveMetrologyConfigurationOnUsagePoint.class), anyVararg())).thenReturn(this.queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(this.effectiveMetrologyConfiguration));
        when(this.effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(this.configuration);
        when(this.effectiveMetrologyConfiguration.getRange()).thenReturn(year2016());
        when(this.effectiveMetrologyConfiguration.getInterval()).thenReturn(Interval.of(year2016()));
        when(this.usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(this.usagePoint.getServiceCategory()).thenReturn(this.serviceCategory);
        when(this.serviceCategory.getCustomPropertySets()).thenReturn(Collections.emptyList());
        when(this.meteringDataModelService.getThesaurus()).thenReturn(this.thesaurus);
    }

    /**
     * Tests the case where introspection is requested for a
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
        service.introspect(this.usagePoint, this.contract, aggregationPeriod);

        //Asserts: see expected exception rule
    }

    /**
     * Tests the case where introspection is requested for a
     * {@link MetrologyContract} that is not active on the {@link UsagePoint}
     * because another {@link MetrologyConfiguration} has been applied
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
        service.introspect(this.usagePoint, this.contract, aggregationPeriod);

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

        // Business method
        MetrologyContractCalculationIntrospector introspector = service.introspect(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        assertThat(introspector).isNotNull();
        assertThat(introspector.getUsagePoint()).isEqualTo(this.usagePoint);
        assertThat(introspector.getMetrologyContract()).isEqualTo(this.contract);
        List<MetrologyContractCalculationIntrospector.ChannelUsage> channelUsages = introspector.getChannelUsagesFor(netConsumption);
        assertThat(channelUsages).isNotNull();
        assertThat(channelUsages).hasSize(2);
        List<Channel> usedChannels =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getChannel)
                        .collect(Collectors.toList());
        assertThat(usedChannels).containsOnly(chn1, chn2);
        Set<Range<Instant>> usedRanges =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getRange)
                        .collect(Collectors.toSet());
        assertThat(usedRanges).containsOnly(year2016());    // Remember that meter activations are clipped to aggregation period
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

        // Business method
        MetrologyContractCalculationIntrospector introspector = service.introspect(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        assertThat(introspector).isNotNull();
        assertThat(introspector.getUsagePoint()).isEqualTo(this.usagePoint);
        assertThat(introspector.getMetrologyContract()).isEqualTo(this.contract);
        List<MetrologyContractCalculationIntrospector.ChannelUsage> channelUsages = introspector.getChannelUsagesFor(netConsumption);
        assertThat(channelUsages).isNotNull();
        assertThat(channelUsages).hasSize(2);
        List<Channel> usedChannels =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getChannel)
                        .collect(Collectors.toList());
        assertThat(usedChannels).containsOnly(chn1, chn2);
        Set<Range<Instant>> usedRanges =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getRange)
                        .collect(Collectors.toSet());
        assertThat(usedRanges).containsOnly(year2016());    // Remember that meter activations are clipped to aggregation period
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
     * Jan 1st 2015 -> Feb 1st 2015
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
        when(this.effectiveMetrologyConfiguration.getRange()).thenReturn(year2015AndBeyond());
        when(this.effectiveMetrologyConfiguration.getInterval()).thenReturn(Interval.of(year2015AndBeyond()));
        Range<Instant> aggregationPeriod = year2015AndBeyond();
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
        when(meterActivation1.getRange()).thenReturn(jan2015());
        when(meterActivation1.getEnd()).thenReturn(feb1st2015());
        when(meterActivation1.overlaps(aggregationPeriod)).thenReturn(true);
        MeterActivation meterActivation2 = mock(MeterActivation.class);
        ChannelsContainer channelsContainer2 = mock(ChannelsContainer.class);
        when(meterActivation2.getChannelsContainer()).thenReturn(channelsContainer2);
        when(meterActivation2.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation2.getMeterRole()).thenReturn(Optional.of(this.meterRole));
        when(meterActivation2.getMultiplier(any(MultiplierType.class))).thenReturn(Optional.empty());
        when(meterActivation2.getInterval()).thenReturn(Interval.startAt(feb1st2015()));
        when(meterActivation2.getRange()).thenReturn(atLeastFeb2015());
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
        when(consumption.getMatchingChannelsFor(channelsContainer2)).thenReturn(Collections.singletonList(chnFeb1));
        when(production.getMatchingChannelsFor(channelsContainer2)).thenReturn(Collections.singletonList(chnFeb2));

        // Business method
        MetrologyContractCalculationIntrospector introspector = service.introspect(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        assertThat(introspector).isNotNull();
        assertThat(introspector.getUsagePoint()).isEqualTo(this.usagePoint);
        assertThat(introspector.getMetrologyContract()).isEqualTo(this.contract);
        List<MetrologyContractCalculationIntrospector.ChannelUsage> channelUsages = introspector.getChannelUsagesFor(netConsumption);
        assertThat(channelUsages).isNotNull();
        assertThat(channelUsages).hasSize(4);
        List<Channel> usedChannels =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getChannel)
                        .collect(Collectors.toList());
        assertThat(usedChannels).containsOnly(chnJan1, chnJan2, chnFeb1, chnFeb2);
        Set<Range<Instant>> usedRanges =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getRange)
                        .collect(Collectors.toSet());
        assertThat(usedRanges).containsOnly(
                jan2015(),
                atLeastFeb2015());
    }

    /**
     * Tests the availability of multiple channels at the meter:
     * Metrology configuration
     * requirements:
     * A- ::= any Wh with flow = forward (aka consumption)
     * deliverables:
     * netConsumption (daily kWh) ::= A-
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A-(15m) -> 15 min kWh
     * A-(1h) -> 60 min kWh
     * In other words, should pick the 60 min channel.
     */
    @Test
    public void multipleChannelsForSameRequirement() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        Range<Instant> aggregationPeriod = year2016();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        when(consumption.getDimension()).thenReturn(Dimension.ENERGY);
        when(this.configuration.getRequirements()).thenReturn(Collections.singletonList(consumption));
        when(this.configuration.getMeterRoleFor(consumption)).thenReturn(Optional.of(this.meterRole));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mockHourlyReadingType("0.0.7.1.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormulaBuilder formulaBuilder = this.newFormulaBuilder();
        ExpressionNode node = formulaBuilder.requirement(consumption).create();
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(node).when(formula).getExpressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        VirtualReadingTypeRequirement virtualConsumption = mock(VirtualReadingTypeRequirement.class);
        when(virtualConsumption.sqlName()).thenReturn("vrt-consumption");

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
        ChannelContract chn15min = mock(ChannelContract.class);
        when(chn15min.getMainReadingType()).thenReturn(consumptionReadingType15min);
        ReadingType consumptionReadingType60min = this.mockHourlyReadingType("0.0.7.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
        ChannelContract chn60min = mock(ChannelContract.class);
        when(chn60min.getMainReadingType()).thenReturn(consumptionReadingType60min);
        when(consumption.getMatchesFor(channelsContainer)).thenReturn(Collections.singletonList(consumptionReadingType60min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn15min));
        when(consumption.getMatchingChannelsFor(channelsContainer)).thenReturn(Collections.singletonList(chn60min));
        when(virtualConsumption.getPreferredChannel()).thenReturn(chn60min);

        // Business method
        MetrologyContractCalculationIntrospector introspector = service.introspect(this.usagePoint, this.contract, aggregationPeriod);

        // Asserts
        assertThat(introspector).isNotNull();
        assertThat(introspector.getUsagePoint()).isEqualTo(this.usagePoint);
        assertThat(introspector.getMetrologyContract()).isEqualTo(this.contract);
        List<MetrologyContractCalculationIntrospector.ChannelUsage> channelUsages = introspector.getChannelUsagesFor(netConsumption);
        assertThat(channelUsages).isNotNull();
        assertThat(channelUsages).hasSize(1);
        List<Channel> usedChannels =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getChannel)
                        .collect(Collectors.toList());
        assertThat(usedChannels).containsOnly(chn60min);
        Set<Range<Instant>> usedRanges =
                channelUsages
                        .stream()
                        .map(MetrologyContractCalculationIntrospector.ChannelUsage::getRange)
                        .collect(Collectors.toSet());
        assertThat(usedRanges).containsOnly(year2016());    // Remember that meter activations are clipped to aggregation period
    }

    private Range<Instant> atLeastFeb2015() {
        return Interval.startAt(feb1st2015()).toClosedOpenRange();
    }

    private Range<Instant> jan2015() {
        return Interval.of(jan1st2015(), feb1st2015()).toClosedOpenRange();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Instant jan1st2016() {
        return Instant.ofEpochMilli(1451606400000L);
    }

    private Instant feb1st2015() {
        return Instant.ofEpochMilli(1422748800000l);
    }

    private Range<Instant> year2015() {
        return Range.closedOpen(jan1st2015(), jan1st2016());
    }

    private Range<Instant> year2015AndBeyond() {
        return Range.atLeast(jan1st2015());
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

    private ReadingType mockHourlyReadingType(String mRID) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        return readingType;
    }

    private DataAggregationServiceImpl testInstance() {
        return new DataAggregationServiceImpl(
                this.calendarService,
                this.customPropertySetService,
                this.meteringService,
                new InstantTruncaterFactory(this.meteringService),
                SqlBuilderFactoryImpl::new,
                () -> new VirtualFactoryImpl(meteringDataModelService),
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(this.meteringService));
    }

    private ServerFormulaBuilder newFormulaBuilder() {
        return this.metrologyConfigurationService.newFormulaBuilder(Formula.Mode.AUTO);
    }

}