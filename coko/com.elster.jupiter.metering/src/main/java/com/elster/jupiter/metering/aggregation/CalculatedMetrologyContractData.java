/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import java.util.List;

/**
 * Models data that was calculated against a {@link UsagePoint}
 * from the definitions provided by a {@link MetrologyContract}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-01 (16:37)
 */
public interface CalculatedMetrologyContractData {

    /**
     * Gets the {@link UsagePoint} whose data was used
     * to calculate the data contained here.
     *
     * @return The UsagePoint
     */
    UsagePoint getUsagePoint();

    /**
     * Gets the {@link MetrologyContract} whose definitions
     * were used to calculate the data contained here.
     *
     * @return The MetrologyContract
     */
    MetrologyContract getMetrologyContract();

    /**
     * Tests if there is calculated data in any of the
     * {@link ReadingTypeDeliverable}s of the related {@link MetrologyContract}.
     *
     * @return A flag that indicates if there is calculated data
     */
    boolean isEmpty();

    /**
     * Gets the data that was calculated for the specified
     * {@link ReadingTypeDeliverable} from the data provided
     * by the {@link UsagePoint} and the definitions provided
     * by the {@link MetrologyContract}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @return The List of BaseReadingRecord ordered by time, oldest intervals first
     * @throws IllegalArgumentException Thrown if the ReadingTypeDeliverable is not part of the MetrologyContract
     */
    List<? extends BaseReadingRecord> getCalculatedDataFor(ReadingTypeDeliverable deliverable);

}