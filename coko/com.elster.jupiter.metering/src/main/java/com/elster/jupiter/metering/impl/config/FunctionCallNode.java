package com.elster.jupiter.metering.impl.config;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "FCT";

    private Function function;

    public FunctionCallNode() {}

    public FunctionCallNode init(Function function) {
        this.function = function;
        return this;
    }

    public FunctionCallNode(List<AbstractNode> children, Function function) {
        super(children);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

}