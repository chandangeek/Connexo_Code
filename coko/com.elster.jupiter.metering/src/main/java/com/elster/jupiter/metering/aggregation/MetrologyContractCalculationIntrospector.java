/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Introspects the calculation of data for a {@link UsagePoint}
 * from the definitions provided by a {@link MetrologyContract}
 * by returning the actual {@link com.elster.jupiter.metering.Channel}s that
 * will be used to calculate the data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-01 (13:35)
 */
public interface MetrologyContractCalculationIntrospector {

    /**
     * Models the usage of a {@link Channel} in the calculation of aggregated data.
     */
    interface ChannelUsage {
        Channel getChannel();

        ReadingTypeRequirement getRequirement();

        /**
         * The Range during which the Channel was used in the calculation of aggregated data.
         *
         * @return The range
         */
        Range<Instant> getRange();
    }

    interface CalendarUsage {
        Calendar getCalendar();

        /**
         * The Range during which the Calendar was used in the calculation of aggregated data.
         *
         * @return The range
         */
        Range<Instant> getRange();
    }

    /**
     * Gets the {@link UsagePoint} for which data calculation is introspected.
     *
     * @return The UsagePoint
     */
    UsagePoint getUsagePoint();

    /**
     * Gets the {@link MetrologyContract} whose definitions
     * were used to introspec the calculation of data.
     *
     * @return The MetrologyContract
     */
    MetrologyContract getMetrologyContract();

    /**
     * Gets {@link ChannelUsage}s for the specified
     * {@link ReadingTypeDeliverable} for the calculation
     * of the data provided by the {@link UsagePoint} and
     * the definitions provided by the {@link MetrologyContract}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @return The List of ChannelUsage
     * @throws IllegalArgumentException Thrown if the ReadingTypeDeliverable is not part of the MetrologyContract
     */
    List<ChannelUsage> getChannelUsagesFor(ReadingTypeDeliverable deliverable);

    /**
     * Gets {@link CalendarUsage}s for the specified
     * {@link ReadingTypeDeliverable} for the calculation
     * of the data provided by the {@link UsagePoint} and
     * the definitions provided by the {@link MetrologyContract}.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @return The List of CalendarUsage
     * @throws IllegalArgumentException Thrown if the ReadingTypeDeliverable is not part of the MetrologyContract
     */
    List<CalendarUsage> getCalendarUsagesFor(ReadingTypeDeliverable deliverable);

}