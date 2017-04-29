/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link AggregatedChannel.AggregatedIntervalReadingRecord} interface
 * when no actual record was calculated by the {@link com.elster.jupiter.metering.aggregation.DataAggregationService}
 * but the user edited the missing value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-06 (14:15)
 */
class AggregatedReadingIntervalRecordBackedByPersistentIntervalReadingRecord implements AggregatedChannel.AggregatedIntervalReadingRecord {
    private final IntervalReadingRecord persistentRecord;

    AggregatedReadingIntervalRecordBackedByPersistentIntervalReadingRecord(IntervalReadingRecord persistentRecord) {
        this.persistentRecord = persistentRecord;
    }

    @Override
    public boolean wasEdited() {
        return true;
    }

    @Override
    public BigDecimal getOriginalValue() {
        return null;    // No original value since this record replaces a calculated record that was missing
    }

    @Override
    public boolean isPartOfTimeOfUseGap() {
        return false;
    }

    @Override
    public Optional<Event> getTimeOfUseEvent() {
        return Optional.empty();
    }

    @Override
    public IntervalReadingRecord filter(ReadingType readingType) {
        return this.persistentRecord.filter(readingType);
    }

    @Override
    public List<Quantity> getQuantities() {
        return this.persistentRecord.getQuantities();
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return persistentRecord.getQuantity(readingType);
    }

    @Override
    public Quantity getQuantity(int offset) {
        return persistentRecord.getQuantity(offset);
    }

    @Override
    public ReadingType getReadingType() {
        return persistentRecord.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return persistentRecord.getReadingType(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return persistentRecord.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return persistentRecord.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        persistentRecord.setProcessingFlags(flags);
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return persistentRecord.getReadingQualities();
    }

    @Override
    public boolean edited() {
        return persistentRecord.edited();
    }

    @Override
    public boolean wasAdded() {
        return persistentRecord.wasAdded();
    }

    @Override
    public boolean confirmed() {
        return persistentRecord.confirmed();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return persistentRecord.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return persistentRecord.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return persistentRecord.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return persistentRecord.getValue();
    }

    @Override
    public String getSource() {
        return persistentRecord.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return persistentRecord.getTimePeriod();
    }

    @Override
    public boolean hasReadingQuality(ReadingQualityType readingQualityType) {
        return persistentRecord.hasReadingQuality(readingQualityType);
    }

}