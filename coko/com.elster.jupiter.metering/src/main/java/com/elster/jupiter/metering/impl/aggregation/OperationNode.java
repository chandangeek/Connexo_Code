package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that applies
 * an {@link Operator} to two operands (i.e. two other ServerExpressionNodes).
 */
class OperationNode implements ServerExpressionNode {

    private final Operator operator;
    private final ServerExpressionNode operand1;
    private final ServerExpressionNode operand2;
    private final ServerExpressionNode safeDivisor;

    OperationNode(Operator operator, ServerExpressionNode operand1, ServerExpressionNode operand2) {
        this(operator, operand1, operand2, null);
    }

    OperationNode(Operator operator, ServerExpressionNode operand1, ServerExpressionNode operand2, ServerExpressionNode safeDivisor) {
        super();
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.safeDivisor = safeDivisor;
    }

    Operator getOperator() {
        return operator;
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