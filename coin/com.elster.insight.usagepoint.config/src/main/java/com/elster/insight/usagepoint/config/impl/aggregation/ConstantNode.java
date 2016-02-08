package com.elster.insight.usagepoint.config.impl.aggregation;

import java.math.BigDecimal;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode extends AbstractNode implements ServerExpressionNode {

    static String TYPE_IDENTIFIER = "CST";

    private final BigDecimal value;

    public ConstantNode(BigDecimal value) {
        super();
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitConstant(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }

}