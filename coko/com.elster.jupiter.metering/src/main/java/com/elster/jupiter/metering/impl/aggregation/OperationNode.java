package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that applies
 * an {@link Operator} to two operands (i.e. two other ServerExpressionNodes).
 */
class OperationNode implements ServerExpressionNode {

    private final Operator operator;
    private final ServerExpressionNode operand1;
    private final ServerExpressionNode operand2;

    OperationNode(Operator operator, ServerExpressionNode operand1, ServerExpressionNode operand2) {
        super();
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
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

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

}