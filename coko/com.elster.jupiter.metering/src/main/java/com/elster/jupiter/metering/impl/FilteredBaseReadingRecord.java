/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredBaseReadingRecord implements BaseReadingRecord {

    private final BaseReadingRecordImpl filtered;
    private final KPermutation view;

    FilteredBaseReadingRecord(BaseReadingRecordImpl filtered, int... indices) {
        this.filtered = filtered;
        view = new KPermutation(indices);
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return filtered.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        this.filtered.setProcessingFlags(flags);
    }

    @Override
    public ReadingType getReadingType() {
        return view.perform(filtered.getReadingTypes()).get(0);
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return view.perform(filtered.getReadingTypes()).get(offset);
    }

    @Override
    public List<IReadingType> getReadingTypes() {
        return view.perform(filtered.getReadingTypes());
    }

    @Override
    public Instant getReportedDateTime() {
        return filtered.getReportedDateTime();
    }

    @Override
    public Instant getTimeStamp() {
        return filtered.getTimeStamp();
    }

    @Override
    public BigDecimal getValue() {
        Quantity quantity = filtered.getQuantity(getReadingType());
        return quantity == null ? null : quantity.getValue();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return view.perform(filtered.getQuantities()).get(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return filtered.getQuantity(readingType);
    }

    @Override
    public List<Quantity> getQuantities() {
        return view.perform(filtered.getQuantities());
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return filtered.getTimePeriod();
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return filtered.getReadingQualities();
    }
    
    @Override
    public BaseReadingRecord filter(ReadingType readingType) {
        return filtered.filter(readingType);
    }
}
