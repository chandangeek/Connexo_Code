/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    private final IntermediateDimension intermediateDimension;
    private List<ServerExpressionNode> arguments;

    FunctionCallNode(Function function, IntermediateDimension intermediateDimension) {
        this(function, intermediateDimension, Collections.emptyList());
    }

    FunctionCallNode(Function function, IntermediateDimension intermediateDimension, ServerExpressionNode... arguments) {
        this(function, intermediateDimension, Arrays.asList(arguments));
    }

    FunctionCallNode(Function function, IntermediateDimension intermediateDimension, List<ServerExpressionNode> arguments) {
        super();
        this.function = function;
        this.intermediateDimension = intermediateDimension;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    Function getFunction() {
        return function;
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return intermediateDimension;
    }

    List<ServerExpressionNode> getArguments() {
        return this.arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

}