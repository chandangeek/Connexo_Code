package com.elster.jupiter.metering.config;

/**
 * Created by igh on 17/03/2016.
 */
public interface OperationNode extends ExpressionNode {



    Operator getOperator();

    ExpressionNode getLeftOperand();

    ExpressionNode getRightOperand();
}
