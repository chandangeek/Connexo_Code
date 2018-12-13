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
 * Represents {@link ReadingRecord} with validation status.<br>
 * This object is used to put all required reading record data together<br>
 * Data, collected by this class will be represented as {@link RegisterDataInfo} by corresponding factory
 */
public class RegisterReadingWithValidationStatus {

    private ReadingRecord readingRecord;
    private ZonedDateTime readingTimeStamp;
    private ReadingRecord previousReadingRecord;
    private List<? extends ReadingQuality> suspectReadingQualities = new ArrayList<>();

    /**
     * Constructor
     *
     * @param readingTimeStamp {@link ZonedDateTime} time of reading
     * @param readingRecord {@link ReadingRecord} reading record
     */
    RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ReadingRecord readingRecord) {
        this.readingTimeStamp = readingTimeStamp;
        this.readingRecord = readingRecord;
        if (readingRecord != null) {
            suspectReadingQualities = readingRecord.getReadingQualities()
                    .stream()
                    .filter(ReadingQualityRecord::isSuspect)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Method to set previous reading record
     *
     * @param previousReadingRecord {@link ReadingRecord} previous reading record. may be null
     */
    void setPreviousReadingRecord(ReadingRecord previousReadingRecord) {
        this.previousReadingRecord = previousReadingRecord;
    }

    /**
     * Method to provide previous reading record
     *
     * @return {@link Optional} of {@link ReadingRecord} previous reading record
     */
    Optional<ReadingRecord> getPreviousReadingRecord() {
        return previousReadingRecord == null ? Optional.empty() : Optional.of(previousReadingRecord);
    }

    /**
     * Method to provide actual {@link ReadingRecord} reading record
     *
     * @return {@link ReadingRecord} reading record
     */
    ReadingRecord getReadingRecord() {
        return readingRecord;
    }

    /**
     * Method to provide numeric value from reading
     *
     * @return {@link BigDecimal} value
     */
    BigDecimal getValue() {
        return readingRecord.getValue();
    }

    /**
     * Method to provide {@link ValidationStatus} for reading
     *
     * @param lastChecked {@link Instant} representing last checks for register
     * @return {@link ValidationStatus} instance
     */
    ValidationStatus getValidationStatus(Instant lastChecked) {
        if (getTimeStamp().isAfter(lastChecked)) {
            return ValidationStatus.NOT_VALIDATED;
        }
        if (suspectReadingQualities.size() > 0) {
            return ValidationStatus.SUSPECT;
        } else {
            return ValidationStatus.OK;
        }
    }

    /**
     * Method to get reading time
     *
     * @return {@link Instant} time of reading
     */
    Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    /**
     * Method to provide {@link ReadingQuality} suspected reading qualities
     *
     * @return list of suspected {@link ReadingQuality} reading qualities
     */
    List<? extends ReadingQuality> getReadingQualities() {
        return suspectReadingQualities.stream().collect(Collectors.toList());
    }
}
