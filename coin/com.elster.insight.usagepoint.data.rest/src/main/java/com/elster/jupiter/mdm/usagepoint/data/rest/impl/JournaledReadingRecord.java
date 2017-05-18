/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JournaledReadingRecord implements BaseReadingRecord {

    private BaseReadingRecord readingRecord;
    private DataValidationStatus validationStatus;
    private Range<Instant> interval;
    private List<? extends ReadingQualityRecord> readingQualityRecords = new ArrayList<>();

    public JournaledReadingRecord(BaseReadingRecord readingRecord) {
        this.readingRecord = readingRecord;
    }

    public Range<Instant> getInterval() {
        return interval;
    }

    public void setInterval(Range<Instant> interval) {
        this.interval = interval;
    }

    public DataValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(DataValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public BaseReadingRecord getStoredReadingRecord() {
        return readingRecord;
    }

    public void setReadingQualityRecords(List<? extends ReadingQualityRecord> readingQualityRecords) {
        this.readingQualityRecords = readingQualityRecords;
    }

    public List<? extends ReadingQualityRecord> getPersistedReadingQualities() {
        return readingRecord.getReadingQualities();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return readingRecord.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return readingRecord.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return readingRecord.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return readingRecord.getValue();
    }

    @Override
    public String getSource() {
        return readingRecord.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return readingRecord.getTimePeriod();
    }

    @Override
    public List<Quantity> getQuantities() {
        return readingRecord.getQuantities();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return readingRecord.getQuantity(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return readingRecord.getQuantity(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return readingRecord.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return readingRecord.getReadingType();
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return readingRecord.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return readingRecord.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        readingRecord.setProcessingFlags(flags);
    }

    @Override
    public BaseReadingRecord filter(ReadingType readingType) {
        return readingRecord.filter(readingType);
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return readingQualityRecords;
    }
}
