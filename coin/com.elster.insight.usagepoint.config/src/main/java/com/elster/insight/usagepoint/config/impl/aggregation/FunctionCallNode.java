package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.associations.Reference;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode implements ServerExpressionNode {

    private Function function;
    private String name;

    public FunctionCallNode(List<ExpressionNode> children, Function function) {
        super(children);
        this.function = function;
    }

    public FunctionCallNode(List<ExpressionNode> children, String name) {
        super(children);
        this.name = name;
    }

    public FunctionCallNode(List<ExpressionNode> children, ExpressionNode parentNode, Function function) {
        super(children, parentNode);
        this.function = function;
    }

    public FunctionCallNode(List<ExpressionNode> children, ExpressionNode parentNode, String name) {
        super(children, parentNode);
        this.name = name;
    }

    public Function getFunction() {
        return function;
    }

    public String getName() {
        return name;
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