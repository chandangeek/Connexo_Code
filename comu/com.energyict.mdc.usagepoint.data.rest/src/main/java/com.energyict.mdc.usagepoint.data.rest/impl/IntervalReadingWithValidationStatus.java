package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public class IntervalReadingWithValidationStatus {

    private Instant readingTimeStamp;
    private TemporalAmount intervalLength;

    private Optional<IntervalReadingRecord> intervalReadingRecord = Optional.empty();

    public IntervalReadingWithValidationStatus(Instant readingTimeStamp, TemporalAmount intervalLength) {
        this.readingTimeStamp = readingTimeStamp;
        this.intervalLength = intervalLength;
    }

    public void setIntervalReadingRecord(IntervalReadingRecord intervalReadingRecord) {
        this.intervalReadingRecord = Optional.of(intervalReadingRecord);
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

    public ValidationStatus getValidationStatus(Instant lastChecked) {
        if (readingTimeStamp.isAfter(lastChecked)) {
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
