/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class ReadingWithValidationStatus<T extends BaseReadingRecord> {

    private static final TemporalAmount MONTH_INTERVAL_LENGTH = Period.ofMonths(1);
    private static final TemporalAmount YEAR_INTERVAL_LENGTH = Period.ofYears(1);
    private final OutputChannelGeneralValidation outputChannelGeneralValidation;

    private Channel channel;
    private ZonedDateTime readingTimeStamp;
    private TemporalAmount intervalLength;

    private Optional<T> readingRecord = Optional.empty();
    private Optional<T> calculatedReadingRecord = Optional.empty();
    private Optional<T> persistedReadingRecord = Optional.empty();
    private Optional<DataValidationStatus> validationStatus = Optional.empty();

    private ReadingWithValidationStatus(Channel channel, OutputChannelGeneralValidation outputChannelGeneralValidation, ZonedDateTime readingTimeStamp, TemporalAmount intervalLength) {
        this.channel = channel;
        this.outputChannelGeneralValidation = outputChannelGeneralValidation;
        this.readingTimeStamp = readingTimeStamp;
        this.intervalLength = intervalLength;
    }

    private ReadingWithValidationStatus(Channel channel, OutputChannelGeneralValidation outputChannelGeneralValidation, ZonedDateTime readingTimeStamp) {
        this.channel = channel;
        this.outputChannelGeneralValidation = outputChannelGeneralValidation;
        this.readingTimeStamp = readingTimeStamp;
    }

    public Optional<DataValidationStatus> getValidationStatus() {
        return validationStatus;
    }

    public void setReadingRecord(T readingRecord) {
        this.readingRecord = Optional.ofNullable(readingRecord);
    }

    public void setPersistedReadingRecord(T readingRecord) {
        this.persistedReadingRecord = Optional.ofNullable(readingRecord);
    }

    public void setCalculatedReadingRecord(T readingRecord) {
        this.calculatedReadingRecord = Optional.ofNullable(readingRecord);
    }

    public static Builder builder(Channel channel, boolean validationIsActive, Instant lastChecked) {
        return new Builder(channel, new OutputChannelGeneralValidation(validationIsActive, lastChecked));
    }

    public void setValidationStatus(DataValidationStatus validationStatus) {
        this.validationStatus = Optional.of(validationStatus);
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    public Instant getReportedDateTime() {
        return this.readingRecord.map(BaseReading::getReportedDateTime).orElse(null);
    }

    public Optional<Range<Instant>> getTimePeriod() {
        ZonedDateTime intervalStart;
        ZonedDateTime intervalEnd = readingTimeStamp;
        if (intervalLength == null) {
            return Optional.empty();
        }
        if (MONTH_INTERVAL_LENGTH.equals(intervalLength)) {
            Month month = Month.from(intervalEnd);
            if (Month.JANUARY == month) {
                intervalStart = intervalEnd.with(month.minus(1)).with(Year.from(intervalEnd).minusYears(1));
            } else {
                intervalStart = intervalEnd.with(month.minus(1));
            }
        } else if (YEAR_INTERVAL_LENGTH.equals(intervalLength)) {
            intervalStart = intervalEnd.with(Year.from(intervalEnd).minusYears(1));
        } else {
            intervalStart = intervalEnd.minus(intervalLength);
        }
        return Optional.of(Range.openClosed(intervalStart.toInstant(), intervalEnd.toInstant()));
    }

    public Optional<Range<Instant>> getBillingPeriod(){
        return readingRecord.flatMap(BaseReading::getTimePeriod);
    }

    public BigDecimal getValue() {
        return readingRecord.map(BaseReading::getValue).orElse(null);
    }

    public Optional<String> getText() {
        return readingRecord
                .filter(readingRecord -> readingRecord instanceof Reading)
                .map(Reading.class::cast)
                .map(Reading::getText);
    }

    public Optional<BigDecimal> getPersistedValue() {
        return persistedReadingRecord.map(BaseReading::getValue);
    }

    public Optional<BigDecimal> getCalculatedValue() {
        return calculatedReadingRecord.map(BaseReading::getValue);
    }

    public boolean isChannelValidationActive() {
        return this.outputChannelGeneralValidation.isValidationActive;
    }

    public Optional<Instant> getChannelLastChecked() {
        return Optional.ofNullable(this.outputChannelGeneralValidation.lastChecked);
    }

    public Optional<Pair<ReadingModificationFlag, ReadingQualityRecord>> getReadingModificationFlag() {
        return Optional.ofNullable(readingRecord.map(record -> ReadingModificationFlag.getModificationFlagWithQualityRecord(record, channel
                .findReadingQualities()
                .atTimestamp(record.getTimeStamp())
                .collect(), calculatedReadingRecord)).orElse(null));
    }

    public static class Builder {
        private final OutputChannelGeneralValidation outputChannelGeneralValidation;
        private final Channel channel;

        private Builder(Channel channel, OutputChannelGeneralValidation outputChannelGeneralValidation) {
            this.outputChannelGeneralValidation = outputChannelGeneralValidation;
            this.channel = channel;
        }

        public ReadingWithValidationStatus<IntervalReadingRecord> from(ZonedDateTime readingTimeStamp, TemporalAmount intervalLength) {
                return new ReadingWithValidationStatus<>(this.channel, this.outputChannelGeneralValidation, readingTimeStamp, intervalLength);
        }

        public ReadingWithValidationStatus<ReadingRecord> from(ZonedDateTime readingTimeStamp) {
                return new ReadingWithValidationStatus<>(this.channel, this.outputChannelGeneralValidation, readingTimeStamp);
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
