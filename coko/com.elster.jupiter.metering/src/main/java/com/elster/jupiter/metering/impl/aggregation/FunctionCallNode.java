package com.elster.jupiter.metering.impl.aggregation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models a {@link ServerExpressionNode} that calls a function
 * with any number of arguments being other ServerExpressionNodes.
 */
class FunctionCallNode implements ServerExpressionNode {

    private final Function function;
    private List<ServerExpressionNode> arguments;

    FunctionCallNode(Function function) {
        this(function, Collections.emptyList());
    }

    FunctionCallNode(Function function, ServerExpressionNode... arguments) {
        this(function, Arrays.asList(arguments));
    }

    FunctionCallNode(Function function, List<ServerExpressionNode> arguments) {
        super();
        this.function = function;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    Function getFunction() {
        return function;
    }

    List<ServerExpressionNode> getArguments() {
        return this.arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

}