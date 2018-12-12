/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class OverflowReading implements Reading {
    private final Reading overflowing;
    private final BigDecimal checked;

    OverflowReading(Reading overflowing, BigDecimal checked) {
        this.overflowing = overflowing;
        this.checked = checked;
    }

    @Override
    public String getReason() {
        return overflowing.getReason();
    }

    @Override
    public String getReadingTypeCode() {
        return overflowing.getReadingTypeCode();
    }

    @Override
    public String getText() {
        return overflowing.getText();
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
