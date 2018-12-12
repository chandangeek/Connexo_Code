/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.util.units.Dimension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ApplyCurrentAndOrVoltageTransformer} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplyCurrentAndOrVoltageTransformerTest {

    @Mock
    private MeteringService meteringService;
    @Mock
    private MeterActivationSet meterActivationSet;
    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private FullySpecifiedReadingTypeRequirement requirement;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable;
    @Mock
    private ChannelContract preferredChannel;
    @Mock
    private MultiplierType vtMultiplierType;
    @Mock
    private MultiplierType ctMultiplierType;
    @Mock
    private MultiplierType transformerMultiplierType;

    @Before
    public void initializeMocks() {
        VirtualReadingTypeRequirement virtualReadingTypeRequirement = mock(VirtualReadingTypeRequirement.class);
        when(this.virtualFactory.requirementFor(eq(Formula.Mode.AUTO), eq(this.requirement), any(ReadingTypeDeliverable.class), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        when(virtualReadingTypeRequirement.getPreferredChannel()).thenReturn(this.preferredChannel);
        VirtualReadingType readingType = VirtualReadingType.from(IntervalLength.MINUTE15, MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR, Accumulation.DELTADELTA, Commodity.ELECTRICITY_PRIMARY_METERED);
        when(virtualReadingTypeRequirement.getSourceReadingType()).thenReturn(readingType);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.CT)).thenReturn(this.ctMultiplierType);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.VT)).thenReturn(this.vtMultiplierType);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.Transformer)).thenReturn(this.transformerMultiplierType);
        when(this.meterActivationSet.getRange()).thenReturn(Range.atLeast(Instant.EPOCH));
    }

    @Test
    public void noReplacementOfNumericalConstants() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        NumericalConstantNode node = new NumericalConstantNode(BigDecimal.TEN);

        // Business method
        ServerExpressionNode replacement = testInstance.visitConstant(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementOfStringConstants() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        StringConstantNode node = new StringConstantNode("noReplacementOnStringConstants");

        // Business method
        ServerExpressionNode replacement = testInstance.visitConstant(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementOfSqlFragement() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        SqlFragmentNode node = new SqlFragmentNode("noReplacementOfSqlFragement");

        // Business method
        ServerExpressionNode replacement = testInstance.visitSqlFragment(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void recursiveVisitsOnOperationNode() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ServerExpressionNode leftOperand = mock(ServerExpressionNode.class);
        when(leftOperand.getIntermediateDimension()).thenReturn(IntermediateDimension.of(Dimension.DIMENSIONLESS));
        ServerExpressionNode rightOperand = mock(ServerExpressionNode.class);
        when(rightOperand.getIntermediateDimension()).thenReturn(IntermediateDimension.of(Dimension.DIMENSIONLESS));
        OperationNode node = Operator.PLUS.node(leftOperand, rightOperand);

        // Business method
        ServerExpressionNode replacement = testInstance.visitOperation(node);

        // Asserts
        verify(leftOperand).accept(testInstance);
        verify(rightOperand).accept(testInstance);
        assertThat(replacement).isNotSameAs(node);
        assertThat(replacement).isInstanceOf(OperationNode.class);
        assertThat(((OperationNode) replacement).getOperator()).isEqualTo(Operator.PLUS);
    }

    @Test
    public void recursiveVisitsOnSafeDivideOperationNode() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ServerExpressionNode leftOperand = mock(ServerExpressionNode.class);
        when(leftOperand.getIntermediateDimension()).thenReturn(IntermediateDimension.of(Dimension.DIMENSIONLESS));
        ServerExpressionNode rightOperand = mock(ServerExpressionNode.class);
        when(rightOperand.getIntermediateDimension()).thenReturn(IntermediateDimension.of(Dimension.DIMENSIONLESS));
        ServerExpressionNode safeDivisor = mock(ServerExpressionNode.class);
        OperationNode node = Operator.SAFE_DIVIDE.safeNode(leftOperand, rightOperand, safeDivisor);

        // Business method
        ServerExpressionNode replacement = testInstance.visitOperation(node);

        // Asserts
        verify(leftOperand).accept(testInstance);
        verify(rightOperand).accept(testInstance);
        verify(safeDivisor).accept(testInstance);
        assertThat(replacement).isNotSameAs(node);
        assertThat(replacement).isInstanceOf(OperationNode.class);
        assertThat(((OperationNode) replacement).getOperator()).isEqualTo(Operator.SAFE_DIVIDE);
    }

    @Test
    public void recursiveVisitsOnFunctionCallNode() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ServerExpressionNode firstArgument = mock(ServerExpressionNode.class);
        ServerExpressionNode secondArgument = mock(ServerExpressionNode.class);
        FunctionCallNode node = new FunctionCallNode(Function.MAX, IntermediateDimension.of(Dimension.DIMENSIONLESS), firstArgument, secondArgument);

        // Business method
        ServerExpressionNode replacement = testInstance.visitFunctionCall(node);

        // Asserts
        verify(firstArgument).accept(testInstance);
        verify(secondArgument).accept(testInstance);
        assertThat(replacement).isNotSameAs(node);
        assertThat(replacement).isInstanceOf(FunctionCallNode.class);
        assertThat(((FunctionCallNode) replacement).getFunction()).isEqualTo(Function.MAX);
    }

    @Test
    public void noReplacementOfDeliverables() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingTypeDeliverableForMeterActivationSet deliverable = mock(ReadingTypeDeliverableForMeterActivationSet.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualDeliverableNode node = new VirtualDeliverableNode(deliverable);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualDeliverable(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementForMeasurementKindThatDoesNotRelateToCTVT() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("kwhSecondaryMetered");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.DISTANCE);
        when(readingType.getCommodity()).thenReturn(Commodity.NOTAPPLICABLE);
        when(this.requirement.getReadingType()).thenReturn(readingType);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementForPrimaryMetered() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("whPrimaryMetered");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(readingType);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementForSecondaryMeteredOnly() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("kwhSecondaryMetered");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(readingType);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfVTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType voltPrimaryMetered = mock(ReadingType.class);
        when(voltPrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltPrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltPrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(voltPrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltPrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.VOLTAGE);
        when(voltPrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(voltPrimaryMetered);
        ReadingType voltSecondaryMetered = mock(ReadingType.class);
        when(voltSecondaryMetered.getMRID()).thenReturn("voltSecondaryMetered");
        when(voltSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(voltSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(voltSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(voltSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(voltSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.VOLTAGE);
        when(voltSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(voltSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(voltSecondaryMetered);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.vtMultiplierType)).thenReturn(Optional.empty());
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfCTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType amperePrimaryMetered = mock(ReadingType.class);
        when(amperePrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(amperePrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(amperePrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(amperePrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(amperePrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.CURRENT);
        when(amperePrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(amperePrimaryMetered);
        ReadingType ampereSecondaryMetered = mock(ReadingType.class);
        when(ampereSecondaryMetered.getMRID()).thenReturn("ampereSecondaryMetered");
        when(ampereSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(ampereSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(ampereSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(ampereSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(ampereSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.CURRENT);
        when(ampereSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(ampereSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(ampereSecondaryMetered);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.ctMultiplierType)).thenReturn(Optional.empty());
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfOnlyCTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType kWhPrimaryMetered = mock(ReadingType.class);
        when(kWhPrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhPrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhPrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhPrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhPrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhPrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(kWhPrimaryMetered);
        ReadingType kWhSecondaryMetered = mock(ReadingType.class);
        when(kWhSecondaryMetered.getMRID()).thenReturn("kwhSecondaryMetered");
        when(kWhSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivationSet.getMultiplier(this.requirement, this.vtMultiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(this.meterActivationSet.getMultiplier(this.requirement, this.ctMultiplierType)).thenReturn(Optional.empty());
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfOnlyVTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType kWhPrimaryMetered = mock(ReadingType.class);
        when(kWhPrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhPrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhPrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhPrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhPrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhPrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(kWhPrimaryMetered);
        ReadingType kWhSecondaryMetered = mock(ReadingType.class);
        when(kWhSecondaryMetered.getMRID()).thenReturn("kwhSecondaryMetered");
        when(kWhSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivationSet.getMultiplier(this.requirement, this.vtMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivationSet.getMultiplier(this.requirement, this.ctMultiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void tranformerMultiplierPresent() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType kWhPrimaryMetered = mock(ReadingType.class);
        when(kWhPrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhPrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhPrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhPrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhPrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhPrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(kWhPrimaryMetered);
        ReadingType kWhSecondaryMetered = mock(ReadingType.class);
        when(kWhSecondaryMetered.getMRID()).thenReturn("kwhSecondaryMetered");
        when(kWhSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(kWhSecondaryMetered);
        BigDecimal multiplierValue = BigDecimal.valueOf(123L);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.transformerMultiplierType)).thenReturn(Optional.of(multiplierValue));
        when(this.meterActivationSet.getMultiplier(this.requirement, this.vtMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivationSet.getMultiplier(this.requirement, this.ctMultiplierType)).thenReturn(Optional.empty());
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isInstanceOf(OperationNode.class);
        OperationNode operationNode = (OperationNode) replacement;
        assertThat(operationNode.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(operationNode.getLeftOperand()).isInstanceOf(NumericalConstantNode.class);
        NumericalConstantNode multiplierNode = (NumericalConstantNode) operationNode.getLeftOperand();
        assertThat(multiplierNode.getValue()).isEqualByComparingTo(multiplierValue);
        assertThat(operationNode.getRightOperand()).isSameAs(node);
        verify(this.meterActivationSet, never()).getMultiplier(this.requirement, this.vtMultiplierType);
        verify(this.meterActivationSet, never()).getMultiplier(this.requirement, this.ctMultiplierType);
    }

    @Test
    public void bothCTandVTPresent() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType kWhPrimaryMetered = mock(ReadingType.class);
        when(kWhPrimaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhPrimaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhPrimaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhPrimaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhPrimaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhPrimaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(kWhPrimaryMetered);
        ReadingType kWhSecondaryMetered = mock(ReadingType.class);
        when(kWhSecondaryMetered.getMRID()).thenReturn("kwhSecondaryMetered");
        when(kWhSecondaryMetered.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(kWhSecondaryMetered.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(kWhSecondaryMetered.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(kWhSecondaryMetered.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(kWhSecondaryMetered.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(kWhSecondaryMetered.getCommodity()).thenReturn(Commodity.ELECTRICITY_SECONDARY_METERED);
        when(this.requirement.getReadingType()).thenReturn(kWhSecondaryMetered);
        when(this.preferredChannel.getMainReadingType()).thenReturn(kWhSecondaryMetered);
        BigDecimal ctMultiplierValue = BigDecimal.valueOf(123L);
        BigDecimal vtMultiplierValue = BigDecimal.valueOf(456L);
        when(this.meterActivationSet.getMultiplier(this.requirement, this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivationSet.getMultiplier(this.requirement, this.vtMultiplierType)).thenReturn(Optional.of(vtMultiplierValue));
        when(this.meterActivationSet.getMultiplier(this.requirement, this.ctMultiplierType)).thenReturn(Optional.of(ctMultiplierValue));
        VirtualRequirementNode node = new VirtualRequirementNode(Formula.Mode.AUTO, this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivationSet);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isInstanceOf(OperationNode.class);
        OperationNode multiplierApplicationNode = (OperationNode) replacement;
        assertThat(multiplierApplicationNode.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(multiplierApplicationNode.getRightOperand()).isSameAs(node);
        assertThat(multiplierApplicationNode.getLeftOperand()).isInstanceOf(OperationNode.class);
        OperationNode multiplierNode = (OperationNode) multiplierApplicationNode.getLeftOperand();
        NumericalConstantNode ctMultiplierNode = (NumericalConstantNode) multiplierNode.getLeftOperand();
        assertThat(ctMultiplierNode.getValue()).isEqualByComparingTo(ctMultiplierValue);
        NumericalConstantNode vtMultiplierNode = (NumericalConstantNode) multiplierNode.getRightOperand();
        assertThat(vtMultiplierNode.getValue()).isEqualByComparingTo(vtMultiplierValue);
    }

    private ApplyCurrentAndOrVoltageTransformer getTestInstance() {
        return new ApplyCurrentAndOrVoltageTransformer(this.meteringService, this.meterActivationSet);
    }

}