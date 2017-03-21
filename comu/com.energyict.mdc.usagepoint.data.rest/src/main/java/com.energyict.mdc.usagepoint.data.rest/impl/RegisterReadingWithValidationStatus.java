/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents {@link ReadingRecord} with validation status
 */
public class RegisterReadingWithValidationStatus {

    private Optional<ReadingRecord> readingRecord = Optional.empty();
    private ZonedDateTime readingTimeStamp;

    public RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ReadingRecord readingRecord) {
        this.readingTimeStamp = readingTimeStamp;
        this.readingRecord = Optional.of(readingRecord);
    }

    public BigDecimal getValue() {
        return readingRecord.map(ReadingRecord::getValue).orElse(null);
    }

    public ValidationStatus getValidationStatus(Instant lastChecked) {
        if (getTimeStamp().isAfter(lastChecked)) {
            return ValidationStatus.NOT_VALIDATED;
        }
        if (readingRecord.isPresent()) {
            List<? extends ReadingQualityRecord> readingQualities = readingRecord.get().getReadingQualities();
            if (readingQualities.stream().anyMatch(ReadingQualityRecord::isSuspect)) {
                return ValidationStatus.SUSPECT;
            }
        }
        return ValidationStatus.OK;
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }
}
