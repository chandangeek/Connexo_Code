/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.units.Dimension;

/**
 * Models a {@link ServerExpressionNode} that applies
 * an {@link Operator} to two operands (i.e. two other ServerExpressionNodes).
 */
class OperationNode implements ServerExpressionNode {

    private final Operator operator;
    private final IntermediateDimension intermediateDimension;
    private final ServerExpressionNode operand1;
    private final ServerExpressionNode operand2;
    private final ServerExpressionNode safeDivisor;

    OperationNode(Operator operator, Dimension dimension, ServerExpressionNode operand1, ServerExpressionNode operand2) {
        this(operator, IntermediateDimension.of(dimension), operand1, operand2, null);
    }

    OperationNode(Operator operator, IntermediateDimension intermediateDimension, ServerExpressionNode operand1, ServerExpressionNode operand2) {
        this(operator, intermediateDimension, operand1, operand2, null);
    }

    OperationNode(Operator operator, IntermediateDimension intermediateDimension, ServerExpressionNode operand1, ServerExpressionNode operand2, ServerExpressionNode safeDivisor) {
        super();
        this.operator = operator;
        this.intermediateDimension = intermediateDimension;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.safeDivisor = safeDivisor;
    }

    Operator getOperator() {
        return operator;
    }

    public IntermediateDimension getIntermediateDimension() {
        return intermediateDimension;
    }

    ServerExpressionNode getLeftOperand() {
        return this.operand1;
    }

    ServerExpressionNode getRightOperand() {
        return this.operand2;
    }

    ServerExpressionNode getSafeDivisor() {
        return this.safeDivisor;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

}