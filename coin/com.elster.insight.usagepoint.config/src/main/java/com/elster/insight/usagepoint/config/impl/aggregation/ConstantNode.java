package com.elster.insight.usagepoint.config.impl.aggregation;

import java.math.BigDecimal;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode implements ExpressionNode {

    private BigDecimal value;

    @Override
    public void accept(Visitor visitor) {

    }
}
