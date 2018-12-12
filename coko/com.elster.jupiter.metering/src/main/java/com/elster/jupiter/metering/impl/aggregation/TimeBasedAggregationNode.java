/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that calls the appropriate
 * aggregation function and aggregation level that matches
 * the {@link com.elster.jupiter.metering.ReadingType} of the
 * {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}
 * or an {@link IntervalLength} defined by the user.
 */
class TimeBasedAggregationNode implements ServerExpressionNode {

    private final AggregationFunction function;
    private final ServerExpressionNode expression;
    private final IntervalLength intervalLength;

    TimeBasedAggregationNode(ServerExpressionNode expression, VirtualReadingType targetReadingType) {
        this(expression, targetReadingType.aggregationFunction(), targetReadingType.getIntervalLength());
    }

    TimeBasedAggregationNode(ServerExpressionNode expression, AggregationFunction aggregationFunction, IntervalLength intervalLength) {
        super();
        this.function = aggregationFunction;
        this.expression = expression;
        this.intervalLength = intervalLength;
    }

    AggregationFunction getFunction() {
        return function;
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return this.getAggregatedExpression().getIntermediateDimension();
    }

    ServerExpressionNode getAggregatedExpression() {
        return this.expression;
    }

    public IntervalLength getIntervalLength() {
        return this.intervalLength;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitTimeBasedAggregation(this);
    }

}