package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode extends AbstractNode implements ExpressionNode {

    private final BigDecimal value;

    public ConstantNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, BigDecimal value) {
        super(parent, children);
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitConstant(this);
    }

}
