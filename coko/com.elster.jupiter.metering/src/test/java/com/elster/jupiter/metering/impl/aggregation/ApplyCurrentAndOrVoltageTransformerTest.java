package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;

import java.math.BigDecimal;
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
    private MeterActivation meterActivation;
    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private ReadingTypeRequirement requirement;
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
        when(this.virtualFactory.requirementFor(eq(this.requirement), any(ReadingTypeDeliverable.class), any(VirtualReadingType.class))).thenReturn(virtualReadingTypeRequirement);
        when(virtualReadingTypeRequirement.getPreferredChannel()).thenReturn(this.preferredChannel);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.CT)).thenReturn(this.ctMultiplierType);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.VT)).thenReturn(this.vtMultiplierType);
        when(this.meteringService.getMultiplierType(MultiplierType.StandardType.Transformer)).thenReturn(this.transformerMultiplierType);
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
    public void noReplacementOfVariableReferences() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        VariableReferenceNode node = new VariableReferenceNode("noReplacementOfVariableReferences");

        // Business method
        ServerExpressionNode replacement = testInstance.visitVariable(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void recursiveVisitsOnOperationNode() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ServerExpressionNode leftOperand = mock(ServerExpressionNode.class);
        ServerExpressionNode rightOperand = mock(ServerExpressionNode.class);
        OperationNode node = new OperationNode(Operator.PLUS, leftOperand, rightOperand);

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
    public void recursiveVisitsOnFunctionCallNode() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ServerExpressionNode firstArgument = mock(ServerExpressionNode.class);
        ServerExpressionNode secondArgument = mock(ServerExpressionNode.class);
        FunctionCallNode node = new FunctionCallNode(Function.MAX, firstArgument, secondArgument);

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
        ReadingTypeDeliverableForMeterActivation deliverable = mock(ReadingTypeDeliverableForMeterActivation.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(deliverable.getReadingType()).thenReturn(readingType);
        VirtualDeliverableNode node = new VirtualDeliverableNode(this.virtualFactory, deliverable);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualDeliverable(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementForMeasurementKindThatDoesNotRelateToCTVT() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.DISTANCE);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfVTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.VOLT);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.VOLTAGE);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivation.getMultiplier(this.vtMultiplierType)).thenReturn(Optional.empty());

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfCTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.CURRENT);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivation.getMultiplier(this.ctMultiplierType)).thenReturn(Optional.empty());

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfOnlyCTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivation.getMultiplier(this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivation.getMultiplier(this.vtMultiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(this.meterActivation.getMultiplier(this.ctMultiplierType)).thenReturn(Optional.empty());

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void noReplacementIfOnlyVTIsMissing() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        when(this.meterActivation.getMultiplier(this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivation.getMultiplier(this.vtMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivation.getMultiplier(this.ctMultiplierType)).thenReturn(Optional.of(BigDecimal.TEN));

        // Business method
        ServerExpressionNode replacement = testInstance.visitVirtualRequirement(node);

        // Asserts
        assertThat(replacement).isSameAs(node);
    }

    @Test
    public void tranformerMultiplierPresent() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        BigDecimal multiplierValue = BigDecimal.valueOf(123L);
        when(this.meterActivation.getMultiplier(this.transformerMultiplierType)).thenReturn(Optional.of(multiplierValue));
        when(this.meterActivation.getMultiplier(this.vtMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivation.getMultiplier(this.ctMultiplierType)).thenReturn(Optional.empty());

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
        verify(this.meterActivation, never()).getMultiplier(this.vtMultiplierType);
        verify(this.meterActivation, never()).getMultiplier(this.ctMultiplierType);
    }

    @Test
    public void bothCTandVTPresent() {
        ApplyCurrentAndOrVoltageTransformer testInstance = this.getTestInstance();
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ENERGY);
        when(this.readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        VirtualRequirementNode node = new VirtualRequirementNode(this.virtualFactory, this.requirement, this.readingTypeDeliverable, this.meterActivation);
        when(this.preferredChannel.getMainReadingType()).thenReturn(readingType);
        BigDecimal ctMultiplierValue = BigDecimal.valueOf(123L);
        BigDecimal vtMultiplierValue = BigDecimal.valueOf(456L);
        when(this.meterActivation.getMultiplier(this.transformerMultiplierType)).thenReturn(Optional.empty());
        when(this.meterActivation.getMultiplier(this.vtMultiplierType)).thenReturn(Optional.of(vtMultiplierValue));
        when(this.meterActivation.getMultiplier(this.ctMultiplierType)).thenReturn(Optional.of(ctMultiplierValue));

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
        return new ApplyCurrentAndOrVoltageTransformer(this.meteringService, this.meterActivation);
    }

}