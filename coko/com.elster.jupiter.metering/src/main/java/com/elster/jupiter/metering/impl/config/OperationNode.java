package com.elster.jupiter.metering.impl.config;

import java.util.Arrays;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "OPR";

    private Operator operator;

    public OperationNode() {}

    public OperationNode(Operator operator, ExpressionNode operand1, ExpressionNode operand2) {
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

}