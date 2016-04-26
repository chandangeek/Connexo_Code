package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models a {@link ServerExpressionNode} that calls the appropriate
 * aggregation function and aggregation level that matches
 * the {@link com.elster.jupiter.metering.ReadingType} of the
 * {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}.
 */
class TimeBasedAggregationNode implements ServerExpressionNode {

    private final AggregationFunction function;
    private final ServerExpressionNode expression;
    private final VirtualReadingType targetReadingType;

    TimeBasedAggregationNode(ServerExpressionNode expression, VirtualReadingType targetReadingType) {
        super();
        this.function = targetReadingType.aggregationFunction();
        this.expression = expression;
        this.targetReadingType = targetReadingType;
    }

    AggregationFunction getFunction() {
        return function;
    }

    ServerExpressionNode getAggregatedExpression() {
        return this.expression;
    }

    public VirtualReadingType getTargetReadingType() {
        return targetReadingType;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitTimeBasedAggregation(this);
    }

}