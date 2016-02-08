package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode implements ServerExpressionNode {

    private final Operator operator;

    public OperationNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, Operator operator) {
        super(parent, children);
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitOperation(this);
    }

}