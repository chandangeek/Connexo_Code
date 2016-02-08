package com.elster.insight.usagepoint.config.impl.aggregation;

import java.util.Arrays;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode {

    static String TYPE_IDENTIFIER = "OP";

    private final Operator operator;

    public OperationNode(Operator operator, AbstractNode operand1, AbstractNode operand2) {
        super(Arrays.asList(operand1, operand2));
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getLeftOperand() {
        return this.getChildren().get(0);
    }

    public ExpressionNode getRightOperand() {
        return this.getChildren().get(1);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitOperation(this);
    }

}