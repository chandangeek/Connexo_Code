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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ZeroIntervalReadingImpl implements IntervalReadingRecord {
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
        // the class is used in custom SAP data selector, which always forces 1 hour reading export and overrides reading type interval with it.
        return Optional.of(Range.openClosed(timeStamp.minus(1, ChronoUnit.HOURS), timeStamp));
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return Collections.emptyList();
    }

    @Override
    public List<Quantity> getQuantities() {
        return Collections.singletonList(doGetQuantity());
    }

    @Override
    public Quantity getQuantity(int offset) {
        return doGetQuantity();
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return doGetQuantity();
    }

    private Quantity doGetQuantity() {
        return readingType.getUnit().getUnit().amount(BigDecimal.ZERO, readingType.getMultiplier().getMultiplier());
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
        return Collections.singletonList(readingType);
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return new ProcessStatus(0);
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntervalReadingRecord filter(ReadingType readingType) {
        return this;
    }
}
