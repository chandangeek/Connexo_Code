package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class IntervalReadingWithValidationStatus {

    private Instant readingTimeStamp;
    private TemporalAmount intervalLength;

    private Optional<IntervalReadingRecord> intervalReadingRecord = Optional.empty();
    private Optional<DataValidationStatus> validationStatus = Optional.empty();

    public IntervalReadingWithValidationStatus(Instant readingTimeStamp, TemporalAmount intervalLength) {
        this.readingTimeStamp = readingTimeStamp;
        this.intervalLength = intervalLength;
    }

    public Optional<DataValidationStatus> getValidationStatus() {
        return validationStatus;
    }

    public void setIntervalReadingRecord(IntervalReadingRecord intervalReadingRecord) {
        this.intervalReadingRecord = Optional.of(intervalReadingRecord);
    }

    public void setValidationStatus(DataValidationStatus validationStatus) {
        this.validationStatus = Optional.of(validationStatus);
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp;
    }

    public Range<Instant> getTimePeriod() {
        return Range.openClosed(readingTimeStamp.minus(intervalLength), readingTimeStamp);
    }

    public BigDecimal getValue() {
        return intervalReadingRecord.map(IntervalReadingRecord::getValue).orElse(null);
    }
}
