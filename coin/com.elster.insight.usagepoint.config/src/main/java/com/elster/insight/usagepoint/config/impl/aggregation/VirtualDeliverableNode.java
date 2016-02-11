package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

/**
 * Provides an implementation for the {@link ExpressionNode} interface
 * for a reference to a {@link ReadingTypeDeliverableForMeterActivation}.
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
class VirtualDeliverableNode extends AbstractNode {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeDeliverableForMeterActivation deliverable;
    private IntervalLength targetInterval;
    private VirtualReadingTypeDeliverable virtualDeliverable;

    VirtualDeliverableNode(VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivation deliverable) {
        super();
        this.virtualFactory = virtualFactory;
        this.deliverable = deliverable;
        this.targetInterval = IntervalLength.from(this.deliverable.getReadingType());
    }

    void setTargetInterval(IntervalLength targetInterval) {
        this.targetInterval = targetInterval;
    }

    /**
     * Finishes the building process of this VirtualDeliverableNode.
     */
    void finish() {
        this.virtualize();
    }

    private void virtualize() {
        this.virtualDeliverable = this.virtualFactory.deliverableFor(this.deliverable, this.targetInterval);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        throw new UnsupportedOperationException("ExpressionNode.Visitor is not expected to visit expression trees that contain VirtualDeliverableNodes");
    }

    @Override
    public <T> T accept(ServerVisitor<T> visitor) {
        return visitor.visitVirtualDeliverable(this);
    }

}