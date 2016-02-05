package com.elster.insight.usagepoint.config.impl.aggregation;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ArgumentListNode implements ExpressionNode {

    private List<ExpressionNode> expression;

    @Override
    public void accept(Visitor visitor) {

    }
}
