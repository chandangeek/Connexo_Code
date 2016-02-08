package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode implements ServerExpressionNode {

    private final Function function;

    public FunctionCallNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, Function function) {
        super(parent, children);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

}