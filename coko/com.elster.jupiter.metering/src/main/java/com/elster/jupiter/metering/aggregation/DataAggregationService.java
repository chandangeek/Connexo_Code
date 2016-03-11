package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Provides data aggregation services that are currently
 * designed for {@link UsagePoint}s. Support for
 * service delivery point and zone will be introduced.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:31)
 */
public interface DataAggregationService {

    /**
     * Calculates the measurement data of the specified {@link MetrologyContract}
     * using the measurement data that is provided by the meters that have been
     * activated on the specified {@link UsagePoint}.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param period The period in time that should be taken into consideration
     * @return The List of BaseReadingRecord ordered by time, oldest intervals first
     */
    List<? extends BaseReadingRecord> calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period);

}