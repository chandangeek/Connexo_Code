/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents {@link ReadingRecord} with validation status. <br>
 * Data, collected by this class will be represented as {@link RegisterDataInfo} by corresponding factory
 */
public class RegisterReadingWithValidationStatus {

    private ReadingRecord readingRecord;
    private ZonedDateTime readingTimeStamp;
    private ReadingRecord previousReadingRecord;
    private List<? extends ReadingQuality> suspectReadingQualities = new ArrayList<>();

    public RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ReadingRecord readingRecord) {
        this.readingTimeStamp = readingTimeStamp;
        this.readingRecord = readingRecord;
        if (readingRecord != null) {
            suspectReadingQualities = readingRecord.getReadingQualities()
                    .stream()
                    .filter(ReadingQualityRecord::isSuspect)
                    .collect(Collectors.toList());
        }
    }

    public void setPreviousReadingRecord(ReadingRecord previousReadingRecord) {
        this.previousReadingRecord = previousReadingRecord;
    }

    public Optional<ReadingRecord> getPreviousReadingRecord() {
        return previousReadingRecord == null ? Optional.empty() : Optional.of(previousReadingRecord);
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
        if (suspectReadingQualities.size() > 0) {
            return ValidationStatus.SUSPECT;
        } else {
            return ValidationStatus.OK;
        }
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    public List<? extends ReadingQuality> getReadingQualities() {
        return suspectReadingQualities.stream().collect(Collectors.toList());
    }
}
