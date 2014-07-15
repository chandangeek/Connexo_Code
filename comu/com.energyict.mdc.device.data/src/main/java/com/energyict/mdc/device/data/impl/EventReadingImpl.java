package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.EventReading;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Provides an implementation for the {@link EventReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:39)
 */
public class EventReadingImpl extends NumericalReadingImpl implements EventReading {

    protected EventReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected EventReadingImpl(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        super(actualReading, readingQualities);
    }

    @Override
    public Optional<Interval> getInterval() {
        return Optional.fromNullable(this.getActualReading().getTimePeriod());
    }

}