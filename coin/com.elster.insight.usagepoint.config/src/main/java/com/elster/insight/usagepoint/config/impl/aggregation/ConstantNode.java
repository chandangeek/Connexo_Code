package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class ConstantNode extends AbstractNode implements ServerExpressionNode {

    private final BigDecimal value;

    public ConstantNode(List<ExpressionNode> children, BigDecimal value) {
        super(children);
        this.value = value;
    }

    public ConstantNode(List<ExpressionNode> children, ExpressionNode parentNode, BigDecimal value) {
        super(children, parentNode);
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
