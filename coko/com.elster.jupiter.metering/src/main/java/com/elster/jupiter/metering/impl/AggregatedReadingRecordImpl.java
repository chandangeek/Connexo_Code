/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.CalculatedReadingRecord;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link AggregatedChannel.AggregatedIntervalReadingRecord} interface
 * as a wrapper for a {@link CalculatedReadingRecord} that was calculated by the {@link com.elster.jupiter.metering.aggregation.DataAggregationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-06 (14:19)
 */
class AggregatedReadingRecordImpl implements AggregatedChannel.AggregatedIntervalReadingRecord, ReadingRecord {

    private final CalculatedReadingRecord record;
    private final Channel persistedChannel;

    AggregatedReadingRecordImpl(Channel persistedChannel, CalculatedReadingRecord record) {
        this.record = record;
        this.persistedChannel = persistedChannel;
    }

    @Override
    public boolean wasEdited() {
        return false;
    }

    @Override
    public BigDecimal getOriginalValue() {
        return null;
    }

    @Override
    public boolean isPartOfTimeOfUseGap() {
        return this.record.isPartOfTimeOfUseGap();
    }

    @Override
    public Optional<Event> getTimeOfUseEvent() {
        return this.record.getTimeOfUseEvent();
    }

    @Override
    public List<Quantity> getQuantities() {
        return record.getQuantities();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return record.getQuantity(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return record.getQuantity(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return record.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return record.getReadingType(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return record.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return record.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        // do nothing as a workaround because this method is called from com.elster.jupiter.validation.impl.ChannelValidator.setValidationQuality()
        // during validation and leads to UnsupportedOperationException thrown by com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecord.setProcessingFlags()
    }

    @Override
    public AggregatedReadingRecordImpl filter(ReadingType readingType) {
        return this;
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return record.getReadingQualities();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return record.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return record.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return this.record.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return record.getValue();
    }

    @Override
    public String getSource() {
        return record.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return record.getTimePeriod();
    }

    @Override
    public String getReason() {
        return null;
    }

    @Override
    public String getReadingTypeCode() {
        return persistedChannel.getMainReadingType().getMRID();
    }

    @Override
    public String getText() {
        return null;
    }
}