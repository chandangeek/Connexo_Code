package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.config.AbstractNode;
import com.elster.jupiter.metering.impl.config.ConstantNode;
import com.elster.jupiter.metering.impl.config.Function;
import com.elster.jupiter.metering.impl.config.FunctionCallNode;
import com.elster.jupiter.metering.impl.config.OperationNode;
import com.elster.jupiter.metering.impl.config.Operator;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;

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
        ConstantNode node = new ConstantNode(BigDecimal.TEN);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) copied).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyMinimumFunctionCallNodeWithoutArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        FunctionCallNode node = new FunctionCallNode(Collections.emptyList(), Function.MIN);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getName()).isNull();
        assertThat(copiedFunctionCallNode.getFunction()).isEqualTo(Function.MIN);
        assertThat(copiedFunctionCallNode.getChildren()).isEmpty();
    }

    @Test
    public void copyFunctionCallNodeWithoutArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(Collections.emptyList(), functionName);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getFunction()).isNull();
        assertThat(copiedFunctionCallNode.getChildren()).isEmpty();
    }

    @Test
    public void copyFunctionCallNodeWithOneArgument() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(Collections.singletonList(new ConstantNode(BigDecimal.TEN)), functionName);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getFunction()).isNull();
        assertThat(copiedFunctionCallNode.getChildren()).hasSize(1);
        AbstractNode onlyChild = copiedFunctionCallNode.getChildren().get(0);
        assertThat(onlyChild).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) onlyChild).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyFunctionCallNodeWithTwoArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(Arrays.asList(new ConstantNode(BigDecimal.TEN), new ConstantNode(BigDecimal.ZERO)), functionName);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getFunction()).isNull();
        assertThat(copiedFunctionCallNode.getChildren()).hasSize(2);
        AbstractNode firstArgument = copiedFunctionCallNode.getChildren().get(0);
        assertThat(firstArgument).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        AbstractNode secondArgument = copiedFunctionCallNode.getChildren().get(1);
        assertThat(secondArgument).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyFunctionCallNodeWithThreeArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(
                Arrays.asList(
                        new ConstantNode(BigDecimal.TEN),
                        new ConstantNode(BigDecimal.valueOf(1000L)),
                        new ConstantNode(BigDecimal.ONE)),
                functionName);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getFunction()).isNull();
        assertThat(copiedFunctionCallNode.getChildren()).hasSize(3);
        AbstractNode firstArgument = copiedFunctionCallNode.getChildren().get(0);
        assertThat(firstArgument).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) firstArgument).getValue()).isEqualTo(BigDecimal.TEN);
        AbstractNode secondArgument = copiedFunctionCallNode.getChildren().get(1);
        assertThat(secondArgument).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) secondArgument).getValue()).isEqualTo(BigDecimal.valueOf(1000L));
        AbstractNode thirdArgument = copiedFunctionCallNode.getChildren().get(2);
        assertThat(thirdArgument).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) thirdArgument).getValue()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void copyPlusOperation() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        OperationNode node = new OperationNode(Operator.PLUS, new ConstantNode(BigDecimal.TEN), new ConstantNode(BigDecimal.ZERO));

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        AbstractNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) leftOperand).getValue()).isEqualTo(BigDecimal.TEN);
        AbstractNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) rightOperand).getValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void copyComplexTree() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        OperationNode node =
                new OperationNode(
                    Operator.PLUS,
                    new ReadingTypeRequirementNode(mock(ReadingTypeRequirement.class)),
                    new FunctionCallNode(Arrays.asList(
                            new ReadingTypeDeliverableNode(readingTypeDeliverable),
                            new ConstantNode(BigDecimal.TEN)),
                        Function.MAX));
        ReadingTypeDeliverableForMeterActivation readingTypeDeliverableForMeterActivation =
                new ReadingTypeDeliverableForMeterActivation(
                        readingTypeDeliverable,
                        this.meterActivation,
                        Range.all(),
                        1,
                        mock(ServerExpressionNode.class),
                        IntervalLength.MINUTE15);
        when(this.readingTypeDeliverableForMeterActivationProvider.from(readingTypeDeliverable, this.meterActivation)).thenReturn(readingTypeDeliverableForMeterActivation);

        // Business method
        AbstractNode copied = node.accept(visitor);

        // Asserts
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(OperationNode.class);
        OperationNode copiedOperationNode = (OperationNode) copied;
        assertThat(copiedOperationNode.getOperator()).isEqualTo(Operator.PLUS);
        AbstractNode leftOperand = copiedOperationNode.getLeftOperand();
        assertThat(leftOperand).isInstanceOf(VirtualRequirementNode.class);
        AbstractNode rightOperand = copiedOperationNode.getRightOperand();
        assertThat(rightOperand).isInstanceOf(FunctionCallNode.class);
        List<AbstractNode> maxFunctionArguments = rightOperand.getChildren();
        assertThat(maxFunctionArguments).hasSize(2);
        assertThat(maxFunctionArguments.get(0)).isInstanceOf(VirtualDeliverableNode.class);
        assertThat(maxFunctionArguments.get(1)).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) maxFunctionArguments.get(1)).getValue()).isEqualTo(BigDecimal.TEN);
    }

    private CopyAndVirtualizeReferences getTestInstance() {
        return new CopyAndVirtualizeReferences(this.virtualFactory, this.readingTypeDeliverableForMeterActivationProvider, this.deliverable, this.meterActivation);
    }

}