package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class ArgumentListNode implements ExpressionNode {

    private ExpressionNode leftExpression;
    private ArgumentListNode argumentList;
    //used for the right node if it contains 1 element (we don't use an ArgumentListNode then, would be too much overhead)
    private ExpressionNode rightExpression;

    @Override
    public void accept(Visitor visitor) {

    }
}
