/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link AggregatedChannel.AggregatedIntervalReadingRecord} interface
 * for records that were both calculated by the {@link com.elster.jupiter.metering.aggregation.DataAggregationService}
 * and edited by the user, i.e. a persistent {@link IntervalReadingRecord} exists for the <i>same</i> timestamp.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-06 (14:21)
 */
public class EditedAggregatedReadingRecord implements AggregatedChannel.AggregatedIntervalReadingRecord {
    private final AggregatedChannel.AggregatedIntervalReadingRecord calculated;
    private final IntervalReadingRecord persistentRecord;

    EditedAggregatedReadingRecord(AggregatedChannel.AggregatedIntervalReadingRecord calculated, IntervalReadingRecord persistentRecord) {
        this.calculated = calculated;
        this.persistentRecord = persistentRecord;
    }

    @Override
    public boolean wasEdited() {
        return true;
    }

    @Override
    public BigDecimal getOriginalValue() {
        return this.calculated.getValue();
    }

    @Override
    public boolean isPartOfTimeOfUseGap() {
        return this.calculated.isPartOfTimeOfUseGap();
    }

    @Override
    public Optional<Event> getTimeOfUseEvent() {
        return this.calculated.getTimeOfUseEvent();
    }

    @Override
    public Instant getTimeStamp() {
        return this.calculated.getTimeStamp();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return this.calculated.getTimePeriod();
    }

    @Override
    public Instant getReportedDateTime() {
        return this.persistentRecord.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return this.persistentRecord.getValue();
    }

    @Override
    public String getSource() {
        return this.calculated.getSource();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return this.calculated.getSensorAccuracy();
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
    public Quantity getQuantity(int offset) {
        return this.persistentRecord.getQuantity(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return this.persistentRecord.getQuantity(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return this.calculated.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return this.calculated.getReadingType(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return this.calculated.getReadingTypes();
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        List<ReadingQualityRecord> readingQualities = new ArrayList<>();
        readingQualities.addAll(this.calculated.getReadingQualities());
        readingQualities.addAll(this.persistentRecord.getReadingQualities());
        return readingQualities;
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return this.persistentRecord.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        this.persistentRecord.setProcessingFlags(flags);
    }

}