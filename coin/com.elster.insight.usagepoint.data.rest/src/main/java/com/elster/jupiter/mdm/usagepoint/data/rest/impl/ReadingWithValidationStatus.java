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

import static com.elster.jupiter.util.streams.Predicates.not;

public abstract class ReadingWithValidationStatus<T extends BaseReadingRecord> {

    private final ZonedDateTime readingTimeStamp;
    private final ChannelGeneralValidation channelGeneralValidation;

    private T persistedReadingRecord;
    private T calculatedReadingRecord;
    private T previousReadingRecord;
    private DataValidationStatus validationStatus;

    public ReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ChannelGeneralValidation channelGeneralValidation) {
        this.readingTimeStamp = readingTimeStamp;
        this.channelGeneralValidation = channelGeneralValidation;
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

    public void setPreviousReadingRecord(T previousReadingRecord) {
        this.previousReadingRecord = previousReadingRecord;
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

    public BigDecimal getPreviousValue(){
        return getPreviousReading().map(BaseReading::getValue).orElse(null);
    }

    public BigDecimal getDeltaValue(){
        BigDecimal value = getValue();
        BigDecimal previousValue = getPreviousValue();
        if(value != null && previousValue != null) {
            return value.subtract(previousValue);
        }
        return null;
    }

    public Optional<T> getReading() {
        return Optional.ofNullable(persistedReadingRecord)
                .map(Optional::of)
                .orElse(Optional.ofNullable(calculatedReadingRecord));
    }

    public Optional<T> getPreviousReading() {
            return Optional.ofNullable(this.previousReadingRecord);
    }

    public Optional<BigDecimal> getCalculatedValue() {
        return Optional.ofNullable(this.calculatedReadingRecord).map(T::getValue);
    }

    public Optional<Instant> getEventDate() {
        return Optional.of(this.getTimeStamp());
    }
    public boolean isChannelValidationActive() {
        return this.channelGeneralValidation.isValidationActive;
    }

    public Optional<Instant> getChannelLastChecked() {
        return Optional.ofNullable(this.channelGeneralValidation.lastChecked);
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
        if (this.calculatedReadingRecord != null) {
            readingQualities.addAll(this.calculatedReadingRecord.getReadingQualities());
        }
        if (this.validationStatus != null) {
            List<ReadingQuality> persistedReadingQualities = this.validationStatus.getReadingQualities().stream()
                    .map(ReadingQuality.class::cast)
                    .filter(not(this::hasValidatedOkReadingQualityType))
                    .collect(Collectors.toList());
            readingQualities.addAll(persistedReadingQualities);
        }
        return readingQualities;
    }

    private boolean hasValidatedOkReadingQualityType(ReadingQuality readingQuality) {
        ReadingQualityType type = readingQuality.getType();
        return type.category().map(QualityCodeCategory.VALID::equals).orElse(false)
                && type.qualityIndex().map(QualityCodeIndex.VALIDATED::equals).orElse(false);
    }

    protected Optional<T> getPersistedReadingRecord() {
        return Optional.ofNullable(this.persistedReadingRecord);
    }

    protected ZonedDateTime getReadingTimeStamp() {
        return this.readingTimeStamp;
    }

    public static class ChannelGeneralValidation {
        private boolean isValidationActive;
        private Instant lastChecked;

        public ChannelGeneralValidation(boolean isValidationActive, Instant lastChecked) {
            this.isValidationActive = isValidationActive;
            this.lastChecked = lastChecked;
        }
    }
}
