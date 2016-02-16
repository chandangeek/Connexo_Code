package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "FCT";

    private Function function;
    private String name;

    public FunctionCallNode() {}

    public FunctionCallNode init(Function function) {
        this.function = function;
        return this;
    }

    public FunctionCallNode(List<AbstractNode> children, Function function) {
        super(children);
        this.function = function;
    }

    public FunctionCallNode(List<AbstractNode> children, String name) {
        super(children);
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