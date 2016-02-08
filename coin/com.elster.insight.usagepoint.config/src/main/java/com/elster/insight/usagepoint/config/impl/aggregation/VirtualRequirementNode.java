package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.time.temporal.TemporalAmount;
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
    private final TemporalAmountFactory temporalAmountFactory;
    private TemporalAmount targetInterval;
    private VirtualReadingTypeRequirement virtualRequirement;

    VirtualRequirementNode(VirtualFactory virtualFactory, TemporalAmountFactory temporalAmountFactory, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.virtualFactory = virtualFactory;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.temporalAmountFactory = temporalAmountFactory;
        this.targetInterval = temporalAmountFactory.from(this.deliverable.getReadingType());
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
    TemporalAmount getPreferredInterval() {
        // Taking the smallest interval for now
        // Todo: match this against the target
        Optional<TemporalAmount> preferredInterval =
                this.meterActivation
                    .getReadingTypes()
                    .stream()
                    .map(this.temporalAmountFactory::from)
                    .sorted(new TemporalAmountComparator())
                    .findFirst();
        if (preferredInterval.isPresent()) {
            return preferredInterval.get();
        }
        else {
            return this.getTargetInterval();
        }
    }

    /**
     * Tests if the specified {@link TemporalAmount interval}
     * can be supported by looking at the backing channels of
     * the actual {@link ReadingTypeRequirement}.
     *
     * @param interval The interval
     * @return A flag that indicates if the interval is backed by one of the channels
     */
    boolean supportsInterval(TemporalAmount interval) {
        TemporalAmountComparator comparator = new TemporalAmountComparator();
        return this.meterActivation
                    .getReadingTypes()
                    .stream()
                    .map(this.temporalAmountFactory::from)
                    .anyMatch(temporalAmount -> comparator.compare(temporalAmount, interval) < 1);
    }

    TemporalAmount getTargetInterval() {
        return this.targetInterval;
    }

    void setTargetInterval(TemporalAmount targetInterval) {
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