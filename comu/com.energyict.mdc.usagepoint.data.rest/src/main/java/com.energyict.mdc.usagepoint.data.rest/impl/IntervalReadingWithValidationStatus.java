/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public class IntervalReadingWithValidationStatus {

    private static final TemporalAmount MONTH_INTERVAL_LENGTH = Period.ofMonths(1);
    private static final TemporalAmount YEAR_INTERVAL_LENGTH = Period.ofYears(1);

    private ZonedDateTime readingTimeStamp;
    private TemporalAmount intervalLength;

    private Optional<IntervalReadingRecord> intervalReadingRecord = Optional.empty();

    public IntervalReadingWithValidationStatus(ZonedDateTime readingTimeStamp, TemporalAmount intervalLength) {
        this.readingTimeStamp = readingTimeStamp;
        this.intervalLength = intervalLength;
    }

    public void setIntervalReadingRecord(IntervalReadingRecord intervalReadingRecord) {
        this.intervalReadingRecord = Optional.of(intervalReadingRecord);
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    public Range<Instant> getTimePeriod() {
        ZonedDateTime intervalStart;
        ZonedDateTime intervalEnd = readingTimeStamp;
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
        return Range.openClosed(intervalStart.toInstant(), intervalEnd.toInstant());
    }

    public BigDecimal getValue() {
        return intervalReadingRecord.map(IntervalReadingRecord::getValue).orElse(null);
    }

    public ValidationStatus getValidationStatus(Instant lastChecked) {
        if (getTimeStamp().isAfter(lastChecked)) {
            return ValidationStatus.NOT_VALIDATED;
        }
        if (intervalReadingRecord.isPresent()) {
            List<? extends ReadingQualityRecord> readingQualities = intervalReadingRecord.get().getReadingQualities();
            if (readingQualities.stream().anyMatch(ReadingQualityRecord::isSuspect)) {
                return ValidationStatus.SUSPECT;
            }
        }
        return ValidationStatus.OK;
    }
}
