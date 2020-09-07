/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public final class GapsIntervalReadingImpl implements IntervalReadingRecord {
    private final IntervalReadingRecord decorated;
    private final Instant timeStamp;

    public static IntervalReadingRecord intervalReading(IntervalReadingRecord decorated, Instant timeStamp) {
        return new GapsIntervalReadingImpl(decorated, timeStamp);
    }

    private GapsIntervalReadingImpl(IntervalReadingRecord decorated, Instant timeStamp) {
        this.decorated = decorated;
        this.timeStamp = timeStamp;
    }

    @Override
    public List<Quantity> getQuantities() {
        return decorated.getQuantities();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return decorated.getQuantity(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return decorated.getQuantity(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return decorated.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return decorated.getReadingType(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return decorated.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return decorated.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntervalReadingRecord filter(ReadingType readingType) {
        IntervalReadingRecord record = decorated.filter(readingType);
        return new GapsIntervalReadingImpl(record, record.getTimeStamp());
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return decorated.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return decorated.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return decorated.getValue();
    }

    @Override
    public String getSource() {
        return decorated.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        // the class is used in custom SAP data selector, which always forces 1 hour reading export and overrides reading type interval with it.
        return Optional.of(Range.openClosed(timeStamp.minus(1, ChronoUnit.HOURS), timeStamp));
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return decorated.getReadingQualities();
    }
}
