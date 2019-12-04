/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

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

public class ZeroIntervalReadingImpl implements IntervalReadingRecord {

    private final ReadingType readingType;
    private final Instant timeStamp;

    public static IntervalReadingRecord intervalReading(ReadingType readingType, Instant timeStamp) {
        return new ZeroIntervalReadingImpl(readingType, timeStamp);
    }

    private ZeroIntervalReadingImpl(ReadingType readingType, Instant timeStamp) {
        this.readingType = readingType;
        this.timeStamp = timeStamp;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return timeStamp;
    }

    @Override
    public BigDecimal getValue() {
        return BigDecimal.ZERO;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return Optional.empty();
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return new ArrayList<>();
    }

    @Override
    public List<Quantity> getQuantities() {
        return null;
    }

    @Override
    public Quantity getQuantity(int offset) {
        return null;
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return null;
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return readingType;
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return null;
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return null;
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
    }

    @Override
    public IntervalReadingRecord filter(ReadingType readingType) {
        return null;
    }
}
