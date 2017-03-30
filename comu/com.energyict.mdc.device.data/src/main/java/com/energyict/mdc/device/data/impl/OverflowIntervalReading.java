/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class OverflowIntervalReading implements IntervalReading {
    private final IntervalReading overflowing;
    private final BigDecimal checked;

    OverflowIntervalReading(IntervalReading overflowing, BigDecimal checked) {
        this.overflowing = overflowing;
        this.checked = checked;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return overflowing.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return overflowing.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return overflowing.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return checked;
    }

    @Override
    public String getSource() {
        return overflowing.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return overflowing.getTimePeriod();
    }

    @Override
    public List<? extends ReadingQuality> getReadingQualities() {
        return overflowing.getReadingQualities();
    }
}
