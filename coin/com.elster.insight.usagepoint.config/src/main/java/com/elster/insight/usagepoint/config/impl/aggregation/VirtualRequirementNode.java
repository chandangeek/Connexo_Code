package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.time.temporal.TemporalAmount;

/**
 * Provides an implementation for the {@link ExpressionNode} interface
 * for a reference to a {@link VirtualReadingTypeRequirement}.
 *
 * It will participate in generating a "with" clause that will
 * do time based aggregation if the interval of the target reading
 * type &gt; interval of the most appropriate channel.
 * It will therefore also need to participate in the select clause
 * that relates to the {@link ReadingTypeDeliverable} because
 * the available data (column names and types) is different
 * for raw timeseries data and aggregated data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:28)
 */
class VirtualRequirementNode implements ServerExpressionNode {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private TemporalAmount targetInterval;
    private VirtualReadingTypeRequirement virtualRequirement;

    VirtualRequirementNode(VirtualFactory virtualFactory, TemporalAmountFactory temporalAmountFactory, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable) {
        super();
        this.virtualFactory = virtualFactory;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.targetInterval = temporalAmountFactory.from(this.deliverable.getReadingType());
    }

    void setTargetInterval(TemporalAmount targetInterval) {
        this.targetInterval = targetInterval;
    }

    private void virtualize() {
        this.virtualRequirement = this.virtualFactory.requirementFor(this.requirement, this.deliverable, this.targetInterval);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        throw new UnsupportedOperationException("ExpressionNode.Visitor is not expected to visit expression trees that contain VirtualRequirementNodes");
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitVirtualRequirement(this);
    }

}