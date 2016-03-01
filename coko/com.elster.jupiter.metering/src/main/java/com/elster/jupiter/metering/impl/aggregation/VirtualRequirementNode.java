package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.config.ExpressionNode;
import com.elster.jupiter.util.sql.SqlBuilder;

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
class VirtualRequirementNode implements ServerExpressionNode {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivation meterActivation;
    private VirtualReadingType targetReadingType;
    private VirtualReadingTypeRequirement virtualRequirement;

    VirtualRequirementNode(VirtualFactory virtualFactory, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, MeterActivation meterActivation) {
        super();
        this.virtualFactory = virtualFactory;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.meterActivation = meterActivation;
        this.targetReadingType = VirtualReadingType.from(this.deliverable.getReadingType());
    }

    ReadingTypeRequirement getRequirement() {
        return requirement;
    }

    /**
     * Calculates the preferred target reading type,
     * taking all the backing channels of the actual
     * {@link ReadingTypeRequirement} into account.
     *
     * @return The preferred reading type
     */
    VirtualReadingType getPreferredReadingType() {
        /* Preferred interval is the smallest matching reading type
         * that is compatible with the target interval. */
        Optional<VirtualReadingType> preferredInterval = new MatchingChannelSelector(this.requirement, this.meterActivation).getPreferredReadingType(this.getTargetReadingType());
        if (preferredInterval.isPresent()) {
            return preferredInterval.get();
        }
        else {
            return VirtualReadingType.notSupported();
        }
    }

    /**
     * Tests if the specified {@link VirtualReadingType}
     * can be supported by looking at the backing channels of
     * the actual {@link ReadingTypeRequirement}.
     *
     * @param readingType The readingType
     * @return A flag that indicates if the readingType is backed by one of the channels
     */
    boolean supportsInterval(VirtualReadingType readingType) {
        return new MatchingChannelSelector(this.requirement, this.meterActivation).isReadingTypeSupported(readingType);
    }

    VirtualReadingType getTargetReadingType() {
        return this.targetReadingType;
    }

    void setTargetReadingType(VirtualReadingType targetReadingType) {
        this.targetReadingType = targetReadingType;
    }

    void finish() {
        this.ensureVirtualized();
    }

    /**
     * Ensures that the {@link ReadingTypeRequirement} is virtualized
     * @see #virtualize()
     */
    private void ensureVirtualized() {
        if (this.virtualRequirement == null) {
            this.virtualize();
        }
    }

    /**
     * Creates the {@link VirtualReadingTypeRequirement} from the current target interval.
     * Postpone calls as long as possible and avoid changing the target interval after
     * calls to this method.
     * Todo: support changing the target interval with notification to the factory that old requirement is no longer necessary
     */
    private void virtualize() {
        this.virtualRequirement = this.virtualFactory.requirementFor(this.requirement, this.deliverable, this.targetReadingType);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVirtualRequirement(this);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the value of this nodes's {@link ReadingTypeRequirement}.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendTo(SqlBuilder sqlBuilder) {
        this.ensureVirtualized();
        this.virtualRequirement.appendReferenceTo(sqlBuilder);
    }

    String sqlName() {
        this.ensureVirtualized();
        return this.virtualRequirement.sqlName();
    }

}