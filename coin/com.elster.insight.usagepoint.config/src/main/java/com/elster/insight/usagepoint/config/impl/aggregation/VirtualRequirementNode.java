package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.util.Optional;

/**
 * Provides an implementation for the {@link ExpressionNode} interface
 * for a reference to a {@link VirtualReadingTypeRequirement}
 * in an expression tree that defines the calculation for
 * a {@link ReadingTypeDeliverable}.
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
class VirtualRequirementNode extends AbstractNode {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private IntervalLength targetInterval;
    private VirtualReadingTypeRequirement virtualRequirement;

    VirtualRequirementNode(VirtualFactory virtualFactory, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.virtualFactory = virtualFactory;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.targetInterval = IntervalLength.from(this.deliverable.getReadingType());
    }

    ReadingTypeRequirement getRequirement() {
        return requirement;
    }

    /**
     * Calculates the preferred target interval,
     * taking all the backing channels of the actual
     * {@link ReadingTypeRequirement} into account.
     *
     * @return The preferred interval
     */
    IntervalLength getPreferredInterval() {
        /* Preferred interval is the smallest matching reading type
         * that is compatible with the target interval. */
        Optional<IntervalLength> preferredInterval = new MatchingChannelSelector(this.requirement, this.meterActivation).getPreferredInterval(this.getTargetInterval());
        if (preferredInterval.isPresent()) {
            return preferredInterval.get();
        }
        else {
            return IntervalLength.NOT_SUPPORTED;
        }
    }

    /**
     * Tests if the specified {@link IntervalLength}
     * can be supported by looking at the backing channels of
     * the actual {@link ReadingTypeRequirement}.
     *
     * @param interval The interval
     * @return A flag that indicates if the interval is backed by one of the channels
     */
    boolean supportsInterval(IntervalLength interval) {
        return new MatchingChannelSelector(this.requirement, this.meterActivation).isIntervalSupported(interval);
    }

    IntervalLength getTargetInterval() {
        return this.targetInterval;
    }

    void setTargetInterval(IntervalLength targetInterval) {
        this.targetInterval = targetInterval;
    }

    /**
     * Creates the {@link VirtualReadingTypeRequirement} from the current target interval.
     * Postpone calls as long as possible and avoid changing the target interval after
     * calls to this method.
     * Todo: support changing the target interval with notification to the factory that old requirement is no longer necessary
     */
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