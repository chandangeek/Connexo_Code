/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the optimizations that occur when nodes
 * are constructed from an {@link Operator}.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-02-19 (11:23)
 */
@RunWith(MockitoJUnitRunner.class)
public class OperatorNodeOptimizationsTest {

    @Mock
    private ServerExpressionNode node;

    @Before
    public void initializeMocks() {
        when(this.node.getIntermediateDimension()).thenReturn(IntermediateDimension.of(Dimension.DIMENSIONLESS));
    }

    @Test
    public void nodePlusZeroIsNode() {
        // Business method & asserts
        assertThat(Operator.PLUS.node(this.node, BigDecimal.ZERO)).isEqualTo(this.node);
    }

    @Test
    public void zeroPlusNodeIsNode() {
        // Business method & asserts
        assertThat(Operator.PLUS.node(BigDecimal.ZERO, this.node)).isEqualTo(this.node);
    }

    @Test
    public void nodePlusNotZeroReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.PLUS.node(this.node, BigDecimal.TEN)).isNotEqualTo(this.node);
    }

    @Test
    public void notZeroPlusNodeReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.PLUS.node(BigDecimal.TEN, this.node)).isNotEqualTo(this.node);
    }

    @Test
    public void plus() {
        // Business method
        OperationNode node = Operator.PLUS.node(this.node, this.node);

        // Asserts
        assertThat(node).isNotEqualTo(this.node);
        assertThat(node.getOperator()).isEqualTo(Operator.PLUS);
        assertThat(node.getLeftOperand()).isEqualTo(this.node);
        assertThat(node.getRightOperand()).isEqualTo(this.node);
    }

    @Test
    public void nodeMinusZeroIsNode() {
        // Business method & asserts
        assertThat(Operator.MINUS.node(this.node, BigDecimal.ZERO)).isEqualTo(this.node);
    }

    @Test
    public void nodeMinusNotZeroReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.MINUS.node(this.node, BigDecimal.TEN)).isNotEqualTo(this.node);
    }

    @Test
    public void minus() {
        // Business method
        OperationNode node = Operator.MINUS.node(this.node, this.node);

        // Asserts
        assertThat(node).isNotEqualTo(this.node);
        assertThat(node.getOperator()).isEqualTo(Operator.MINUS);
        assertThat(node.getLeftOperand()).isEqualTo(this.node);
        assertThat(node.getRightOperand()).isEqualTo(this.node);
    }

    @Test
    public void nodeMultipliedWithOneIsNode() {
        // Business method & asserts
        assertThat(Operator.MULTIPLY.node(this.node, BigDecimal.ONE)).isEqualTo(this.node);
    }

    @Test
    public void oneMultipliedWithNodeIsNode() {
        // Business method & asserts
        assertThat(Operator.MULTIPLY.node(BigDecimal.ONE, this.node)).isEqualTo(this.node);
    }

    @Test
    public void nodeMultipliedWithValueReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.MULTIPLY.node(this.node, BigDecimal.TEN)).isNotEqualTo(this.node);
    }

    @Test
    public void valueMultipliedWithNodeReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.MULTIPLY.node(BigDecimal.TEN, this.node)).isNotEqualTo(this.node);
    }

    @Test
    public void nodeMultipliedWithZeroIsAlwaysZero() {
        // Business method
        ServerExpressionNode node = Operator.MULTIPLY.node(this.node, BigDecimal.ZERO);

        // Asserts
        assertThat(node).isInstanceOf(NumericalConstantNode.class);
        NumericalConstantNode zero = (NumericalConstantNode) node;
        assertThat(zero.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void zeroMultipliedWithNodeIsAlwaysZero() {
        // Business method
        ServerExpressionNode node = Operator.MULTIPLY.node(BigDecimal.ZERO, this.node);

        // Asserts
        assertThat(node).isInstanceOf(NumericalConstantNode.class);
        NumericalConstantNode zero = (NumericalConstantNode) node;
        assertThat(zero.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void multiply() {
        // Business method
        OperationNode node = Operator.MULTIPLY.node(this.node, this.node);

        // Asserts
        assertThat(node).isNotEqualTo(this.node);
        assertThat(node.getOperator()).isEqualTo(Operator.MULTIPLY);
        assertThat(node.getLeftOperand()).isEqualTo(this.node);
        assertThat(node.getRightOperand()).isEqualTo(this.node);
    }

    @Test(expected = ArithmeticException.class)
    public void nodeDivideByZero() {
        Operator.DIVIDE.node(this.node, BigDecimal.ZERO);
    }

    @Test
    public void nodeDivideByOneIsNode() {
        // Business method & asserts
        assertThat(Operator.DIVIDE.node(this.node, BigDecimal.ONE)).isEqualTo(this.node);
    }

    @Test
    public void nodeDivideByValueReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.DIVIDE.node(this.node, BigDecimal.TEN)).isNotEqualTo(this.node);
    }

    @Test
    public void valueDivideByNodeReturnsNewNode() {
        // Business method & asserts
        assertThat(Operator.DIVIDE.node(BigDecimal.TEN, this.node)).isNotEqualTo(this.node);
    }

    @Test
    public void zeroDividedByNodeIsAlwaysZero() {
        // Business method
        ServerExpressionNode node = Operator.DIVIDE.node(BigDecimal.ZERO, this.node);

        // Asserts
        assertThat(node).isInstanceOf(NumericalConstantNode.class);
        NumericalConstantNode zero = (NumericalConstantNode) node;
        assertThat(zero.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void divide() {
        // Business method
        OperationNode node = Operator.DIVIDE.node(this.node, this.node);

        // Asserts
        assertThat(node).isNotEqualTo(this.node);
        assertThat(node.getOperator()).isEqualTo(Operator.DIVIDE);
        assertThat(node.getLeftOperand()).isEqualTo(this.node);
        assertThat(node.getRightOperand()).isEqualTo(this.node);
    }

}