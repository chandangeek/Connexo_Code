/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.units.Quantity;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class IntervalReadingImpl implements IntervalReading {

    private final IntervalReadingRecord decorated;
    private final ReadingType readingType;

    public static IntervalReading intervalReading(IntervalReadingRecord decorated, ReadingType readingType) {
        return new IntervalReadingImpl(decorated, readingType);
    }

    private IntervalReadingImpl(IntervalReadingRecord decorated, ReadingType readingType) {
        this.decorated = decorated;
        this.readingType = readingType;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return decorated.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return decorated.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return decorated.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        Quantity qty = decorated.getQuantity(readingType);
        return qty == null ? null : decorated.getQuantity(readingType).getValue();
    }

    @Override
    public String getSource() {
        return decorated.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return decorated.getTimePeriod();
    }

    @Override
    public List<? extends ReadingQuality> getReadingQualities() {
        return decorated.getReadingQualities();
    }
}
