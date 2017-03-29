/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents {@link ReadingRecord} with validation status. <br>
 * Data, collected by this class will be represented as {@link RegisterDataInfo} by corresponding factory
 */
public class RegisterReadingWithValidationStatus {

    private ReadingRecord readingRecord;
    private ZonedDateTime readingTimeStamp;
    private ReadingRecord previousReadingRecord;

    public RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ReadingRecord readingRecord) {
        this.readingTimeStamp = readingTimeStamp;
        this.readingRecord = readingRecord;
    }

    public void setPreviousReadingRecord(ReadingRecord previousReadingRecord) {
        this.previousReadingRecord = previousReadingRecord;
    }

    public Optional<ReadingRecord> getPreviousReadingRecord() {
        return Optional.of(previousReadingRecord);
    }

    public ReadingRecord getReadingRecord() {
        return readingRecord;
    }

    public BigDecimal getValue() {
        return readingRecord.getValue();
    }

    public ValidationStatus getValidationStatus(Instant lastChecked) {
        if (getTimeStamp().isAfter(lastChecked)) {
            return ValidationStatus.NOT_VALIDATED;
        }
        if (readingRecord != null) {
            List<? extends ReadingQualityRecord> readingQualities = readingRecord.getReadingQualities();
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
