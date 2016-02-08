package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode implements ExpressionNode {

    private final Function function;

    public FunctionCallNode(Reference<ExpressionNode> parent, List<ExpressionNode> children, Function function) {
        super(parent, children);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitFunctionCall(this);
    }

}