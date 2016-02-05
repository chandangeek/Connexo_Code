package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

import java.math.BigDecimal;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void copyConstantNode() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        ConstantNode node = new ConstantNode(BigDecimal.TEN);

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(ConstantNode.class);
        assertThat(((ConstantNode) copied).getValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void copyIdentifierNode() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String expectedName = "copyIdentifierNode";
        IdentifierNode node = new IdentifierNode(expectedName);

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(IdentifierNode.class);
        assertThat(((IdentifierNode) copied).getName()).isEqualTo(expectedName);
    }

    @Test
    public void copyFunctionCallNodeWithoutArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(new IdentifierNode(functionName), null);

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getIdentifier().getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getArgumentList()).isNull();
    }

    @Test
    public void copyFunctionCallNodeWithOneArgument() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        FunctionCallNode node = new FunctionCallNode(new IdentifierNode(functionName), new ArgumentListNode(new ConstantNode(BigDecimal.TEN), null));

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getIdentifier().getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getArgumentList().getLeft()).isInstanceOf(ConstantNode.class);
        assertThat(copiedFunctionCallNode.getArgumentList().getRight()).isNull();
    }

    @Test
    public void copyFunctionCallNodeWithTwoArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        String identifierName = "self";
        FunctionCallNode node = new FunctionCallNode(new IdentifierNode(functionName), new ArgumentListNode(new ConstantNode(BigDecimal.TEN), new IdentifierNode(identifierName)));

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getIdentifier().getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getArgumentList().getLeft()).isInstanceOf(ConstantNode.class);
        assertThat(copiedFunctionCallNode.getArgumentList().getRight()).isInstanceOf(IdentifierNode.class);
    }

    @Test
    public void copyFunctionCallNodeWithThreeArguments() {
        CopyAndVirtualizeReferences visitor = getTestInstance();
        String functionName = "max";
        String identifierName = "self";
        FunctionCallNode node =
                new FunctionCallNode(
                    new IdentifierNode(functionName),
                    new ArgumentListNode(
                            new ConstantNode(BigDecimal.TEN),
                            new ArgumentListNode(
                                    new IdentifierNode(identifierName),
                                    new ConstantNode(BigDecimal.ONE))));

        // Business method
        node.accept(visitor);

        // Asserts
        ExpressionNode copied = visitor.getCopy();
        assertThat(copied).isNotNull();
        assertThat(copied).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode copiedFunctionCallNode = (FunctionCallNode) copied;
        assertThat(copiedFunctionCallNode.getIdentifier().getName()).isEqualTo(functionName);
        assertThat(copiedFunctionCallNode.getArgumentList().getLeft()).isInstanceOf(ConstantNode.class);
        assertThat(copiedFunctionCallNode.getArgumentList().getRight()).isInstanceOf(ArgumentListNode.class);
    }

    private CopyAndVirtualizeReferences getTestInstance() {
        return new CopyAndVirtualizeReferences(this.virtualFactory, this.temporalAmountFactory, this.deliverable);
    }

}