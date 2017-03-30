/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.List;

/**
 * Provides an implementation for the {@link ExpressionNode} interface
 * for a reference to a {@link ReadingTypeDeliverableForMeterActivationSet}.
 * <p>
 * It will participate in generating a "with" clause that will
 * do time based aggregation if the interval of the target reading
 * type &gt; interval of the most appropriate channel.
 * It will therefore also need to participate in the select clause
 * that relates to the {@link ReadingTypeDeliverable} because
 * the available data (column names and types) is different
 * for raw timeseries data and aggregated data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (15:24)
 */
class VirtualDeliverableNode implements ServerExpressionNode {

    private final ReadingTypeDeliverableForMeterActivationSet deliverable;
    private VirtualReadingType targetReadingType;

    VirtualDeliverableNode(ReadingTypeDeliverableForMeterActivationSet deliverable) {
        super();
        this.deliverable = deliverable;
        this.targetReadingType = VirtualReadingType.from(this.deliverable.getReadingType());
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(this.getTargetReadingType().getDimension());
    }

    VirtualReadingType getSourceReadingType() {
        return this.deliverable.getTargetReadingType();
    }

    VirtualReadingType getPreferredReadingType() {
        return this.targetReadingType;
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
        return visitor.visitVirtualDeliverable(this);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the simple value of this nodes's {@link ReadingTypeRequirement}.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendTo(SqlBuilder sqlBuilder) {
        this.deliverable.appendSimpleReferenceTo(sqlBuilder);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the value of this nodes's {@link ReadingTypeRequirement}
     * and apply unit conversion if necessary.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendToWithUnitConversion(SqlBuilder sqlBuilder) {
        this.deliverable.appendReferenceTo(sqlBuilder, this.targetReadingType);
    }

    String sqlName() {
        return this.deliverable.sqlName();
    }

    List<VirtualRequirementNode> nestedRequirements(Visitor<List<VirtualRequirementNode>> visitor) {
        return this.deliverable.nestedRequirements(visitor);
    }

}