/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.DataValidationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ReadingWithValidationStatus<T extends BaseReadingRecord> {

    private final ZonedDateTime readingTimeStamp;

    private T persistedReadingRecord;
    private T calculatedReadingRecord;
    private DataValidationStatus validationStatus;

    public ReadingWithValidationStatus(ZonedDateTime readingTimeStamp) {
        this.readingTimeStamp = readingTimeStamp;
    }

    public void setValidationStatus(DataValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Optional<DataValidationStatus> getValidationStatus() {
        return Optional.ofNullable(this.validationStatus);
    }

    public void setPersistedReadingRecord(T readingRecord) {
        this.persistedReadingRecord = readingRecord;
    }

    public void setCalculatedReadingRecord(T readingRecord) {
        this.calculatedReadingRecord = readingRecord;
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    public Instant getReportedDateTime() {
        return getReading().map(BaseReading::getReportedDateTime).orElse(null);
    }

    public BigDecimal getValue() {
        return getReading().map(BaseReading::getValue).orElse(null);
    }

    public Optional<T> getReading() {
        return Optional.ofNullable(persistedReadingRecord)
                .map(Optional::of)
                .orElse(Optional.ofNullable(calculatedReadingRecord));
    }

    public Optional<BigDecimal> getCalculatedValue() {
        return Optional.ofNullable(this.calculatedReadingRecord).map(T::getValue);
    }

    public Optional<Instant> getEventDate() {
        return Optional.of(this.getTimeStamp());
    }

    public Optional<Pair<ReadingModificationFlag, ReadingQuality>> getReadingModificationFlag() {
        if (this.validationStatus != null) {
            return Optional.ofNullable(
                    ReadingModificationFlag.getModificationFlagWithQualityRecord(
                            this.validationStatus.getReadingQualities(),
                            Optional.ofNullable(this.calculatedReadingRecord)
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    public List<? extends ReadingQuality> getReadingQualities() {
        List<ReadingQuality> readingQualities = new ArrayList<>();
        if (this.calculatedReadingRecord != null && this.persistedReadingRecord == null) {
            readingQualities.addAll(this.calculatedReadingRecord.getReadingQualities());
        }
        if (this.validationStatus != null) {
            List<ReadingQuality> persistedReadingQualities = this.validationStatus.getReadingQualities().stream()
                    .map(ReadingQuality.class::cast)
                    .filter(readingQuality -> !hasValidatedOkReadingQualityType(readingQuality))
                    .collect(Collectors.toList());

            // check if it was Manually accepted (3.10.1)
            if (this.calculatedReadingRecord != null && persistedReadingQualities.stream().anyMatch(type -> "3.10.1".equals(type.getTypeCode()))) {
                readingQualities.addAll(this.calculatedReadingRecord.getReadingQualities());
            }

            readingQualities.addAll(persistedReadingQualities);
        }
        return readingQualities;
    }

    static boolean hasValidatedOkReadingQualityType(ReadingQuality readingQuality) {
        ReadingQualityType type = readingQuality.getType();
        return type.category().map(QualityCodeCategory.VALID::equals).orElse(false)
                && type.qualityIndex().map(QualityCodeIndex.VALIDATED::equals).orElse(false);
    }

    protected Optional<T> getPersistedReadingRecord() {
        return Optional.ofNullable(this.persistedReadingRecord);
    }

}