package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
        OperationNode node =
                new OperationNode(
                    Operator.PLUS,
                    new ReadingTypeRequirementNode(mock(ReadingTypeRequirement.class)),
                    new FunctionCallNode(Arrays.asList(
                            new ReadingTypeDeliverableNode(mock(ReadingTypeDeliverable.class)),
                            new ConstantNode(BigDecimal.TEN)),
                        Function.MAX));

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
        return new CopyAndVirtualizeReferences(this.virtualFactory, this.temporalAmountFactory, this.deliverable, this.meterActivation);
    }

}