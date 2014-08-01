package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.BillingReading;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Provides an implementation for the {@link BillingReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:39)
 */
public class BillingReadingImpl extends NumericalReadingImpl implements BillingReading {

    protected BillingReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected BillingReadingImpl(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        super(actualReading, readingQualities);
    }

    @Override
    public Optional<Interval> getInterval() {
        return Optional.fromNullable(this.getActualReading().getTimePeriod());
    }

}