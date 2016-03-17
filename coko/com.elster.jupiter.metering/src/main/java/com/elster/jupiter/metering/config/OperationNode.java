package com.elster.jupiter.metering.config;

/**
 * Created by igh on 17/03/2016.
 */
public interface OperationNode extends ExpressionNode {

    String TYPE_IDENTIFIER = "OPR";

    Operator getOperator();

    ExpressionNode getLeftOperand();

    ExpressionNode getRightOperand();
}
