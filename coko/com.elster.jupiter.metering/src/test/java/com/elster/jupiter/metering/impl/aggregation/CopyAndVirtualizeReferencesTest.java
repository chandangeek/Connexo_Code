package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CopyAndVirtualizeReferences} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (16:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class CopyAndVirtualizeReferencesTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private TemporalAmountFactory temporalAmountFactory;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingTypeDeliverableForMeterActivationProvider readingTypeDeliverableForMeterActivationProvider;

    @Before
    public void initializeMocks() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.deliverable.getReadingType()).thenReturn(readingType);
    }

    @Test
    public void copyConstantNode() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.ConstantNode node = new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) copied).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyMinimumFunctionCallNodeWithoutArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.FunctionCallNode node =
                new com.elster.jupiter.metering.impl.config.FunctionCallNode(
                        Collections.emptyList(),
                        com.elster.jupiter.metering.impl.config.Function.MIN);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.MIN);
        assertThat(copiedFunctionCallNode.getArguments()).isEmpty();
    }

    @Test
    public void copyFunctionCallNodeWithoutArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.FunctionCallNode node =
                new com.elster.jupiter.metering.impl.config.FunctionCallNode(
                        Collections.emptyList(),
                        com.elster.jupiter.metering.impl.config.Function.MAX);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(com.elster.jupiter.metering.impl.aggregation.Function.MAX);
        assertThat(copiedFunctionCallNode.getArguments()).isEmpty();
    }

    @Test
    public void copyFunctionCallNodeWithOneArgument() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.FunctionCallNode node =
                new com.elster.jupiter.metering.impl.config.FunctionCallNode(
                        Collections.singletonList(
                                new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN)),
                                com.elster.jupiter.metering.impl.config.Function.MIN);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(com.elster.jupiter.metering.impl.aggregation.Function.MIN);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(1);
        ServerExpressionNode onlyChild = copiedFunctionCallNode.getArguments().get(0);
        assertThat(onlyChild).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) onlyChild).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyFunctionCallNodeWithTwoArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.FunctionCallNode node =
                new com.elster.jupiter.metering.impl.config.FunctionCallNode(Arrays.asList(
                        new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN),
                        new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.ZERO)),
                        com.elster.jupiter.metering.impl.config.Function.MAX);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.MAX);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(2);
        ServerExpressionNode firstArgument = copiedFunctionCallNode.getArguments().get(0);
        assertThat(firstArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode secondArgument = copiedFunctionCallNode.getArguments().get(1);
        assertThat(secondArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyFunctionCallNodeWithThreeArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.FunctionCallNode node =
                new com.elster.jupiter.metering.impl.config.FunctionCallNode(Arrays.asList(
                            new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN),
                            new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.valueOf(1000L)),
                            new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.ONE)),
                        com.elster.jupiter.metering.impl.config.Function.MAX);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        com.elster.jupiter.metering.impl.aggregation.FunctionCallNode copiedFunctionCallNode = (com.elster.jupiter.metering.impl.aggregation.FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(com.elster.jupiter.metering.impl.aggregation.Function.MAX);
        assertThat(copiedFunctionCallNode.getArguments()).hasSize(3);
        ServerExpressionNode firstArgument = copiedFunctionCallNode.getArguments().get(0);
        assertThat(firstArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode secondArgument = copiedFunctionCallNode.getArguments().get(1);
        assertThat(secondArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.valueOf(1000L));
        ServerExpressionNode thirdArgument = copiedFunctionCallNode.getArguments().get(2);
        assertThat(thirdArgument).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) thirdArgument).getValue()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void copyPlusOperation() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        com.elster.jupiter.metering.impl.config.OperationNode node = new com.elster.jupiter.metering.impl.config.OperationNode(
                com.elster.jupiter.metering.impl.config.Operator.PLUS,
                new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN),
                new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.ZERO));

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        ServerExpressionNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) leftOperand).getValue()).isEqualTo(BigDecimal.TEN);
        ServerExpressionNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) rightOperand).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyComplexTree() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        com.elster.jupiter.metering.impl.config.OperationNode node =
                new com.elster.jupiter.metering.impl.config.OperationNode(
                        com.elster.jupiter.metering.impl.config.Operator.PLUS,
                    new com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode(mock(ReadingTypeRequirement.class)),
                    new com.elster.jupiter.metering.impl.config.FunctionCallNode(Arrays.asList(
                                new com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNode(readingTypeDeliverable),
                                new com.elster.jupiter.metering.impl.config.ConstantNode(BigDecimal.TEN)),
                            com.elster.jupiter.metering.impl.config.Function.MAX));
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation =
                new ReadingTypeDeliverableForMeterActivation(
                        readingTypeDeliverable,
                        this.meterActivation,
                        Range.all(),
                        1,
                        mock(ServerExpressionNode.class),
                        VirtualReadingType.from(readingType));
        when(this.readingTypeDeliverableForMeterActivationProvider.from(readingTypeDeliverable, this.meterActivation)).thenReturn(readingTypeDeliverableForMeterActivation);

        // Business method
        ServerExpressionNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        ServerExpressionNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(VirtualRequirementNode.class);
        ServerExpressionNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(com.elster.jupiter.metering.impl.aggregation.FunctionCallNode.class);
        com.elster.jupiter.metering.impl.aggregation.FunctionCallNode functionCallNode = (com.elster.jupiter.metering.impl.aggregation.FunctionCallNode) rightOperand;
        List<ServerExpressionNode> maxFunctionArguments = functionCallNode.getArguments();
        assertThat(maxFunctionArguments).hasSize(2);
        assertThat(maxFunctionArguments.get(0)).isInstanceOf(VirtualDeliverableNode.class);
        assertThat(maxFunctionArguments.get(1)).isInstanceOf(NumericalConstantNode.class);
        assertThat(((NumericalConstantNode) maxFunctionArguments.get(1)).getValue()).isEqualTo(BigDecimal.TEN);
    }

    private CopyAndVirtualizeReferences getTestInstance() {
        return new CopyAndVirtualizeReferences(this.virtualFactory, this.readingTypeDeliverableForMeterActivationProvider, this.deliverable, this.meterActivation);
    }

}