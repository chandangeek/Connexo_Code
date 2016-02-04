package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode implements ExpressionNode {

    private IdentifierNode identifier;
    private ArgumentListNode argumentList;

    @Override
    public void accept(Visitor visitor) {

    }
}
