package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class IntervalReadingWithValidationStatus {

    private final OutputChannelGeneralValidation outputChannelGeneralValidation;
    private final Instant readingTimeStamp;
    private final TemporalAmount intervalLength;

    private Optional<IntervalReadingRecord> intervalReadingRecord = Optional.empty();
    private Optional<DataValidationStatus> validationStatus = Optional.empty();

    private IntervalReadingWithValidationStatus(OutputChannelGeneralValidation outputChannelGeneralValidation, Instant readingTimeStamp, TemporalAmount intervalLength) {
        this.outputChannelGeneralValidation = outputChannelGeneralValidation;
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

    public boolean isChannelValidationActive() {
        return this.outputChannelGeneralValidation.isValidationActive;
    }

    public Optional<Instant> getChannelLastChecked() {
        return Optional.ofNullable(this.outputChannelGeneralValidation.lastChecked);
    }

    public static Builder builder(boolean validationIsActive, Instant lastChecked) {
        return new Builder(new OutputChannelGeneralValidation(validationIsActive, lastChecked));
    }

    public static class Builder {
        private final OutputChannelGeneralValidation outputChannelGeneralValidation;

        private Builder(OutputChannelGeneralValidation outputChannelGeneralValidation) {
            this.outputChannelGeneralValidation = outputChannelGeneralValidation;
        }

        public IntervalReadingWithValidationStatus from(Instant readingTimeStamp, TemporalAmount intervalLength) {
            return new IntervalReadingWithValidationStatus(this.outputChannelGeneralValidation, readingTimeStamp, intervalLength);
        }
    }

    private static class OutputChannelGeneralValidation {
        boolean isValidationActive;
        Instant lastChecked;

        private OutputChannelGeneralValidation(boolean isValidationActive, Instant lastChecked) {
            this.isValidationActive = isValidationActive;
            this.lastChecked = lastChecked;
        }
    }
}
