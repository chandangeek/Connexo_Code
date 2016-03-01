package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.config.ExpressionNode;
import com.elster.jupiter.util.sql.SqlBuilder;

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
class VirtualDeliverableNode implements ServerExpressionNode {

    private final VirtualFactory virtualFactory;
    private final ReadingTypeDeliverableForMeterActivation deliverable;
    private VirtualReadingType targetReadingType;
    private VirtualReadingTypeDeliverable virtualDeliverable;

    VirtualDeliverableNode(VirtualFactory virtualFactory, ReadingTypeDeliverableForMeterActivation deliverable) {
        super();
        this.virtualFactory = virtualFactory;
        this.deliverable = deliverable;
        this.targetReadingType = VirtualReadingType.from(this.deliverable.getReadingType());
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
        if (this.virtualDeliverable == null) {
            this.virtualize();
        }
    }

    private void virtualize() {
        this.virtualDeliverable = this.virtualFactory.deliverableFor(this.deliverable, this.targetReadingType);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVirtualDeliverable(this);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the value of this nodes's {@link ReadingTypeRequirement}.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendTo(SqlBuilder sqlBuilder) {
        this.ensureVirtualized();
        this.virtualDeliverable.appendReferenceTo(sqlBuilder);
    }

    String sqlName() {
        this.ensureVirtualized();
        return this.virtualDeliverable.sqlName();
    }

}