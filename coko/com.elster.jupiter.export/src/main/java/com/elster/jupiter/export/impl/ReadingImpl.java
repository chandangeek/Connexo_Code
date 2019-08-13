/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class ReadingImpl implements Reading {

    private final ReadingRecord decorated;
    private final ReadingType readingType;

    public static Reading reading(ReadingRecord decorated, ReadingType readingType) {
        return new ReadingImpl(decorated, readingType);
    }

    private ReadingImpl(ReadingRecord decorated, ReadingType readingType) {
        this.decorated = decorated;
        this.readingType = readingType;
    }

    @Override
    public String getReason() {
        return decorated.getReason();
    }

    @Override
    public String getReadingTypeCode() {
        return readingType.getMRID();
    }

    @Override
    public String getText() {
        return decorated.getText();
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
        return decorated.getQuantity(readingType).getValue();
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
