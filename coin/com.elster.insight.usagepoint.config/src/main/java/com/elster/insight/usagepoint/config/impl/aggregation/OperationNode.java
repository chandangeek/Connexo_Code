package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode implements ExpressionNode {

    private Operator operator;
    private ExpressionNode leftExpression;
    private ExpressionNode rightExpression;

    @Override
    public void accept(Visitor visitor) {

    }
}
