package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Optional;

/**
 * Represents a {@link ReadingTypeDeliverable} for a {@link MeterActivation}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (09:46)
 */
class VirtualReadingTypeDeliverable {

    private final ReadingTypeDeliverableForMeterActivation deliverable;
    private final IntervalLength targetInterval;

    VirtualReadingTypeDeliverable(ReadingTypeDeliverableForMeterActivation deliverable, IntervalLength targetInterval) {
        super();
        this.deliverable = deliverable;
        this.targetInterval = targetInterval;
    }

    ReadingType getReadingType () {
        return this.deliverable.getReadingType();
    }

    void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with("ts7", Optional.empty(), "id", "value", "timestamp", "localdate");
    }

}