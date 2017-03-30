/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Dimension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNodeImpl extends AbstractNode implements FunctionCallNode {

    static final String TYPE_IDENTIFIER = "FCT";

    private Function function;
    private AggregationLevel aggregationLevel;
    private Thesaurus thesaurus;

    // For ORM layer
    @SuppressWarnings("unused")
    public FunctionCallNodeImpl() {
    }

    FunctionCallNodeImpl(List<? extends ServerExpressionNode> children, Function function, AggregationLevel aggregationLevel, Thesaurus thesaurus) {
        super(children);
        this.function = function;
        this.aggregationLevel = aggregationLevel;
        this.thesaurus = thesaurus;
    }

    @Override
    public Function getFunction() {
        return function;
    }

    @Override
    public Optional<AggregationLevel> getAggregationLevel() {
        return Optional.ofNullable(this.aggregationLevel);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(function.toString() + "(");
        result.append(this.getServerSideChildren().stream().map(ServerExpressionNode::toString).collect(Collectors.joining(", ")));
        if (this.aggregationLevel != null) {
            result.append(", ");
            result.append(this.aggregationLevel.name());
        }
        result.append(")");
        return result.toString();
    }

    public void validate() {
        if (this.getParent() == null) {
            if (this.getChildren().isEmpty()) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED);
            }
            AbstractNode first = (AbstractNode) this.getChildren().get(0);
            for (int i = 1; i < this.getChildren().size(); i++) {
                AbstractNode child = (AbstractNode) this.getChildren().get(i);
                if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                        first.getDimension(), child.getDimension())) {
                    throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_FUNCTION_CALL);
                }
            }
        }
    }

    @Override
    public Dimension getDimension() {
        return this.getChildren().get(0).getDimension();
    }

}