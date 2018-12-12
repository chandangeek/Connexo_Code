/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link InferReadingType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (12:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class InferReadingTypeTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeteringDataModelService dataModelService;
    @Mock
    private FullySpecifiedReadingTypeRequirement requirement;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private MeterActivationSet meterActivationSet;

    private VirtualFactory virtualFactory;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        ReadingType readingType = this.mock15minkWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getRange()).thenReturn(Range.all());
        when(this.dataModelService.getThesaurus()).thenReturn(this.thesaurus);
        this.virtualFactory = new VirtualFactoryImpl(this.dataModelService);
    }

    @Test
    public void inferNumericalConstantOnly() {
        InferReadingType infer = this.testInstance();
        NumericalConstantNode node = new NumericalConstantNode(BigDecimal.TEN);

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.dontCare());
    }

    @Test
    public void inferStringConstantOnly() {
        InferReadingType infer = this.testInstance();
        StringConstantNode node = new StringConstantNode("BigDecimal.TEN");

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.dontCare());
    }

    @Test
    public void inferConstantsOnly() {
        InferReadingType infer = this.testInstance();
        OperationNode node =
                Operator.PLUS.node(
                        new NumericalConstantNode(BigDecimal.valueOf(123L)),
                        new FunctionCallNode(
                                Function.MAX,
                                IntermediateDimension.of(Dimension.DIMENSIONLESS), new NumericalConstantNode(BigDecimal.ZERO),
                                new NumericalConstantNode(BigDecimal.TEN)));

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.HOUR1);
    }

    @Test
    public void inferRequirementOnly() {
        InferReadingType infer = this.testInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);    // Different from the target set in test instance
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        ChannelContract channel = mock(ChannelContract.class);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivationSet.getMatchingChannelsFor(this.requirement)).thenReturn(Collections.singletonList(channel));
        VirtualRequirementNode node =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        this.requirement,
                        this.deliverable,
                        this.meterActivationSet);

        // Business method
        VirtualReadingType preferredReadingType = node.accept(infer);

        // Asserts
        assertThat(preferredReadingType.getIntervalLength()).isEqualTo(IntervalLength.MINUTE15);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInOperation() {
        InferReadingType infer = this.testInstance();

        FullySpecifiedReadingTypeRequirement requirement1 = mock(FullySpecifiedReadingTypeRequirement.class);
        ReadingType requirement1ReadingType = mock(ReadingType.class);
        when(requirement1ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement1ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);   // Make sure this is not compatible with requirement 2
        when(requirement1.getReadingType()).thenReturn(requirement1ReadingType);
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        FullySpecifiedReadingTypeRequirement requirement2 = mock(FullySpecifiedReadingTypeRequirement.class);
        ReadingType requirement2ReadingType = mock(ReadingType.class);
        when(requirement2ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement2ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);  // Incompatible with requirement 1
        when(requirement2.getReadingType()).thenReturn(requirement2ReadingType);
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());
        OperationNode sum = Operator.PLUS.node(requirementNode1, requirementNode2);
        OperationNode multiply = Operator.MULTIPLY.node(sum, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        multiply.accept(infer);

        // Asserts: see expected exception rule
    }

    @Test(expected = UnsupportedOperationException.class)
    public void inferIncompatibleRequirementsInFuntionCall() {
        InferReadingType infer = this.testInstance();

        FullySpecifiedReadingTypeRequirement requirement1 = mock(FullySpecifiedReadingTypeRequirement.class);
        ReadingType requirement1ReadingType = mock(ReadingType.class);
        when(requirement1ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement1ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE2);   // Make sure this is not compatible with requirement 2
        when(requirement1.getReadingType()).thenReturn(requirement1ReadingType);
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        FullySpecifiedReadingTypeRequirement requirement2 = mock(FullySpecifiedReadingTypeRequirement.class);
        ReadingType requirement2ReadingType = mock(ReadingType.class);
        when(requirement2ReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(requirement2ReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);  // Incompatible with requirement 1
        when(requirement2.getReadingType()).thenReturn(requirement2ReadingType);
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), requirementNode1,
                        requirementNode2,
                        new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        maximum.accept(infer);

        // Asserts: see expected exception rule
    }

    /**
     * Setup: X = Y + Z
     * Y = requirement that is backed by 15 min kWh and 60 min kWh values
     * Z = requirement that is only backed by 15 min kWh values
     * X explicitly needs 60 min kWh values
     * Initially, Y will be requested to produce 60 min kWh values, which it can.
     * Then the algorithm should discover that Z can only produce 15 min kWh values
     * and even though those are more precise than the hourly values
     * it will prefer to work with the hourly because that matches the target exactly.
     * Since Z can be aggregated to hourly,
     * the inference algorithm will return 60 min kWh as a result.
     */
    @Test
    public void inferCompatibleRequirementsWithSameUnitInOperation() {
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlykWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ChannelContract hourlyChannel = mock(ChannelContract.class);
        when(hourlyChannel.getMainReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minkWhReadingType();
        ChannelContract fifteenMinChannel = mock(ChannelContract.class);
        when(fifteenMinChannel.getMainReadingType()).thenReturn(fifteenMinReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement1)).thenReturn(Arrays.asList(fifteenMinChannel, hourlyChannel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement2)).thenReturn(Collections.singletonList(fifteenMinChannel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());
        OperationNode sum = Operator.PLUS.node(requirementNode1, requirementNode2);
        OperationNode multiply = Operator.MULTIPLY.node(sum, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        VirtualReadingType preferredReadingType = multiply.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode1.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode1.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode2.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode2.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    /**
     * Same setup and purpose as {@link #inferCompatibleRequirementsWithSameUnitInOperation()}.
     */
    @Test
    public void inferCompatibleRequirementsWithSameUnitInFunctionCall() {
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlykWhReadingType();
        ChannelContract hourlyChannel = mock(ChannelContract.class);
        when(hourlyChannel.getMainReadingType()).thenReturn(hourlyReadingType);
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ReadingType fifteenMinReadingType = this.mock15minkWhReadingType();
        ChannelContract fifteenMinChannel = mock(ChannelContract.class);
        when(fifteenMinChannel.getMainReadingType()).thenReturn(fifteenMinReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);

        when(this.meterActivationSet.getMatchingChannelsFor(requirement1)).thenReturn(Arrays.asList(fifteenMinChannel, hourlyChannel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement2)).thenReturn(Collections.singletonList(fifteenMinChannel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), requirementNode1,
                        requirementNode2,
                        new NumericalConstantNode(BigDecimal.TEN));
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());

        // Business method
        VirtualReadingType preferredReadingType = maximum.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode1.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode1.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode2.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode2.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    /**
     * Setup: X = Y + Z
     * Y = requirement that is backed by 15 min Ampère
     * Z = requirement that is backed by 15 min Volt values
     * X explicitly needs 60 min kWh values
     * Assert that the algorithm enforces both interval and multiplier to both X an Y.
     */
    @Test
    public void inferCompatibleRequirementsWithSameDimensionInOperation() {
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlykWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ChannelContract ampereChannel = mock(ChannelContract.class);
        ReadingType ampereReadingType = this.mock15minAmpereReadingType();
        when(ampereChannel.getMainReadingType()).thenReturn(ampereReadingType);
        ChannelContract voltChannel = mock(ChannelContract.class);
        ReadingType voltReadingType = this.mock15minVoltReadingType();
        when(voltChannel.getMainReadingType()).thenReturn(voltReadingType);

        ReadingTypeRequirement y = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(y)).thenReturn(Collections.singletonList(ampereChannel));
        VirtualRequirementNode nodeY =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        y,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement z = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(z)).thenReturn(Collections.singletonList(voltChannel));
        VirtualRequirementNode nodeZ =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        z,
                        this.deliverable,
                        this.meterActivationSet);
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());
        OperationNode sum = Operator.PLUS.node(nodeY, nodeZ);

        // Business method
        VirtualReadingType preferredReadingType = sum.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(nodeY.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(nodeY.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(nodeZ.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(nodeZ.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    /**
     * Same setup and purpose as {@link #inferCompatibleRequirementsWithSameDimensionInOperation()}.
     */
    @Test
    public void inferCompatibleRequirementsWithSameDimensionInFunctionCall() {
        InferReadingType infer = this.testInstance();

        ReadingType hourlyReadingType = this.mockHourlykWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourlyReadingType);
        ChannelContract ampereChannel = mock(ChannelContract.class);
        ReadingType ampereReadingType = this.mock15minAmpereReadingType();
        when(ampereChannel.getMainReadingType()).thenReturn(ampereReadingType);
        ChannelContract voltChannel = mock(ChannelContract.class);
        ReadingType voltReadingType = this.mock15minVoltReadingType();
        when(voltChannel.getMainReadingType()).thenReturn(voltReadingType);

        ReadingTypeRequirement y = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(y)).thenReturn(Collections.singletonList(ampereChannel));
        VirtualRequirementNode nodeY =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        y,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement z = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(z)).thenReturn(Collections.singletonList(voltChannel));
        VirtualRequirementNode nodeZ =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        z,
                        this.deliverable,
                        this.meterActivationSet);
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), nodeY,
                        nodeZ,
                        new NumericalConstantNode(BigDecimal.TEN));
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());

        // Business method
        VirtualReadingType preferredReadingType = maximum.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(nodeY.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(nodeY.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.AMPERE, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(nodeZ.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.HOUR1, MetricMultiplier.KILO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(nodeZ.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.ZERO, ReadingTypeUnit.VOLT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    /**
     * Setup: X = Y + Z
     * Y = requirement that is backed by 15 min kW and 60 min kW values
     * Z = requirement that is only backed by 15 min kWh values
     * X explicitly needs 60 min kWh values
     */
    @Test
    public void inferCompatibleRequirementsWithFlowVolumeConversionInOperation() {
        InferReadingType infer = this.testInstance();

        ReadingType fifteenMin_kW_ReadingType = this.mock15minkWReadingType();
        ReadingType fifteenMin_kWh_ReadingType = this.mock15minkWhReadingType();
        ReadingType hourly_kW_ReadingType = this.mockHourlykWReadingType();
        ReadingType hourly_kWh_ReadingType = this.mockHourlykWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourly_kWh_ReadingType);
        ChannelContract hourly_kWh_Channel = mock(ChannelContract.class);
        when(hourly_kWh_Channel.getMainReadingType()).thenReturn(hourly_kWh_ReadingType);
        ChannelContract hourly_kW_Channel = mock(ChannelContract.class);
        when(hourly_kW_Channel.getMainReadingType()).thenReturn(hourly_kW_ReadingType);
        ChannelContract fifteenMin_kW_Channel = mock(ChannelContract.class);
        when(fifteenMin_kW_Channel.getMainReadingType()).thenReturn(fifteenMin_kW_ReadingType);
        ChannelContract fifteenMin_kWh_Channel = mock(ChannelContract.class);
        when(fifteenMin_kWh_Channel.getMainReadingType()).thenReturn(fifteenMin_kWh_ReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement1)).thenReturn(Arrays.asList(fifteenMin_kW_Channel, hourly_kW_Channel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement2)).thenReturn(Collections.singletonList(fifteenMin_kWh_Channel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());
        OperationNode sum = Operator.PLUS.node(requirementNode1, requirementNode2);
        OperationNode multiply = Operator.MULTIPLY.node(sum, new NumericalConstantNode(BigDecimal.TEN));

        // Business method
        VirtualReadingType preferredReadingType = multiply.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode1.getTargetReadingType())
                .isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode1.getPreferredChannel().getMainReadingType()))
                .isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode2.getTargetReadingType())
                .isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode2.getPreferredChannel().getMainReadingType()))
                .isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    /**
     * Same setup and purpose as {@link #inferCompatibleRequirementsWithFlowVolumeConversionInOperation()}.
     */
    @Test
    public void inferCompatibleRequirementsWithFlowVolumeConversionInFunctionCall() {
        InferReadingType infer = this.testInstance();

        ReadingType fifteenMin_kW_ReadingType = this.mock15minkWReadingType();
        ReadingType fifteenMin_kWh_ReadingType = this.mock15minkWhReadingType();
        ReadingType hourly_kW_ReadingType = this.mockHourlykWReadingType();
        ReadingType hourly_kWh_ReadingType = this.mockHourlykWhReadingType();
        when(this.deliverable.getReadingType()).thenReturn(hourly_kWh_ReadingType);
        ChannelContract hourly_kWh_Channel = mock(ChannelContract.class);
        when(hourly_kWh_Channel.getMainReadingType()).thenReturn(hourly_kWh_ReadingType);
        ChannelContract hourly_kW_Channel = mock(ChannelContract.class);
        when(hourly_kW_Channel.getMainReadingType()).thenReturn(hourly_kW_ReadingType);
        ChannelContract fifteenMin_kW_Channel = mock(ChannelContract.class);
        when(fifteenMin_kW_Channel.getMainReadingType()).thenReturn(fifteenMin_kW_ReadingType);
        ChannelContract fifteenMin_kWh_Channel = mock(ChannelContract.class);
        when(fifteenMin_kWh_Channel.getMainReadingType()).thenReturn(fifteenMin_kWh_ReadingType);

        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement1)).thenReturn(Arrays.asList(fifteenMin_kW_Channel, hourly_kW_Channel));
        VirtualRequirementNode requirementNode1 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement1,
                        this.deliverable,
                        this.meterActivationSet);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(this.meterActivationSet.getMatchingChannelsFor(requirement2)).thenReturn(Collections.singletonList(fifteenMin_kWh_Channel));
        VirtualRequirementNode requirementNode2 =
                new VirtualRequirementNode(
                        Formula.Mode.AUTO,
                        this.virtualFactory,
                        requirement2,
                        this.deliverable,
                        this.meterActivationSet);
        FunctionCallNode maximum =
                new FunctionCallNode(
                        Function.MAX,
                        IntermediateDimension.of(Dimension.DIMENSIONLESS), requirementNode1,
                        requirementNode2,
                        new NumericalConstantNode(BigDecimal.TEN));
        this.virtualFactory.nextMeterActivationSet(this.meterActivationSet, Range.all());

        // Business method
        VirtualReadingType preferredReadingType = maximum.accept(infer);

        // Asserts
        assertThat(preferredReadingType).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode1.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode1.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATT, Accumulation.BULKQUANTITY, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(requirementNode2.getTargetReadingType()).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
        assertThat(VirtualReadingType.from(requirementNode2.getPreferredChannel()
                .getMainReadingType())).isEqualTo(VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED));
    }

    private ReadingType mock15minkWhReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-15m-kWh");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15minkWReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-15m-kW");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15minAmpereReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-15m-A");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mock15minVoltReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-15m-V");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mockHourlykWhReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-60m-kWh");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private ReadingType mockHourlykWReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("InferReadingType-60m-kW");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

    private InferReadingType testInstance() {
        return new InferReadingType(this.hourlyVirtualReadingType());
    }

    private VirtualReadingType hourlyVirtualReadingType() {
        return VirtualReadingType.from(this.mock60MinkWh());
    }

    private ReadingType mock60MinkWh() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        return readingType;
    }

}