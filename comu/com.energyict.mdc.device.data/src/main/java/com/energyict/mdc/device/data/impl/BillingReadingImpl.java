package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.google.common.collect.Range;

import com.energyict.mdc.device.data.BillingReading;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public Optional<Range<Instant>> getRange() {
        return this.getActualReading().getTimePeriod();
    }

}