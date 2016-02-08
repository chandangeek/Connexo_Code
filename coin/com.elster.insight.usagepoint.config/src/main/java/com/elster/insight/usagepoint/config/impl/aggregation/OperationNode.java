package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode implements ExpressionNode {

    private final Operator operator;

    public OperationNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, Operator operator) {
        super(parent, children);
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOperation(this);
    }

}