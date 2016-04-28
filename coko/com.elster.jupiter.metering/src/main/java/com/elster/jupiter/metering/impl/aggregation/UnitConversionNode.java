package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;

/**
 * Models a {@link ServerExpressionNode} that will
 * apply unit conversion to another ServerExpressionNode
 * and visitor components should treat this as a single unit.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-28 (09:47)
 */
class UnitConversionNode implements ServerExpressionNode {

    private final ServerExpressionNode expressionNode;
    private VirtualReadingType targetReadingType;

    UnitConversionNode(ServerExpressionNode expressionNode, VirtualReadingType targetReadingType) {
        this.expressionNode = expressionNode;
        this.targetReadingType = targetReadingType;
    }

    ServerExpressionNode getExpressionNode() {
        return expressionNode;
    }

    VirtualReadingType getTargetReadingType() {
        return targetReadingType;
    }

    void setTargetReadingType(VirtualReadingType targetReadingType) {
        this.targetReadingType = targetReadingType;
    }

    void setTargetIntervalLength(IntervalLength intervalLength) {
        this.setTargetReadingType(this.targetReadingType.withIntervalLength(intervalLength));
    }

    void setTargetMultiplier(MetricMultiplier multiplier) {
        this.setTargetReadingType(this.targetReadingType.withMetricMultiplier(multiplier));
    }

    void setTargetCommodity(Commodity commodity) {
        this.setTargetReadingType(this.targetReadingType.withCommondity(commodity));
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnitConversion(this);
    }

}