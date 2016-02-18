package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.sql.SqlBuilder;

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

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.deliverable.appendDefinitionTo(sqlBuilder);
    }

    String sqlName() {
        return this.deliverable.sqlName();
    }

    void appendReferenceTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
    }

}