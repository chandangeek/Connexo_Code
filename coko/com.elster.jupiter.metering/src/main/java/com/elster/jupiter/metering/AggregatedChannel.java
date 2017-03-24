/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.aggregation.DataAggregationService;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface AggregatedChannel extends Channel {

    List<AggregatedIntervalReadingRecord> getCalculatedIntervalReadings(Range<Instant> interval);

    List<ReadingRecord> getCalculatedRegisterReadings(Range<Instant> interval);

    List<IntervalReadingRecord> getPersistedIntervalReadings(Range<Instant> interval);

    List<ReadingRecord> getPersistedRegisterReadings(Range<Instant> interval);

    @ProviderType
    interface AggregatedIntervalReadingRecord extends IntervalReadingRecord {
        /**
         * Tests if this record was produced by the {@link DataAggregationService}
         * to replace data that was filtered out because time of use was applied
         * to the {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}.
         * Not replacing it, would not allow the client code to distinguish
         * missing records from records that were filtered out as a result of time of use.
         *
         * @return <code>true</code> iff this record is part of a time of use gap
         */
        boolean isPartOfTimeOfUseGap();

        /**
         * Gets the time of use {@link Event} that cause the actual record data to be filtered out.
         * The event's code will be different from the time of use value of the records reading type.
         *
         * @return The Event or <code>Optional.empty()</code> if this record is not part of a time of use gap
         * @see #isPartOfTimeOfUseGap()
         */
        Optional<Event> getTimeOfUseEvent();
    }

}
