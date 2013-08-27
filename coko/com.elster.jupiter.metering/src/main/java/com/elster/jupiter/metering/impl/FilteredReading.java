package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.collections.KPermutation;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class FilteredReading implements IntervalReading {

    private final IntervalReading source;
    private final KPermutation view;

    public FilteredReading(IntervalReading source, int... indices) {
        this.source = source;
        view = new KPermutation(indices);
    }

    @Override
    public long getProcessingFlags() {
        return source.getProcessingFlags();
    }

    @Override
    public ReadingType getReadingType() {
        return source.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return source.getReadingType(offset);
    }

    @Override
    public List<ReadingType> getReadingTypes() {
        return view.perform(source.getReadingTypes());
    }

    @Override
    public Date getReportedDateTime() {
        return source.getReportedDateTime();
    }

    @Override
    public Date getTimeStamp() {
        return source.getTimeStamp();
    }

    @Override
    public BigDecimal getValue() {
        return source.getValue();
    }

    @Override
    public BigDecimal getValue(int offset) {
        return view.perform(source.getValues()).get(offset);
    }

    @Override
    public BigDecimal getValue(ReadingType readingType) {
        return source.getValue(readingType);
    }

    @Override
    public List<BigDecimal> getValues() {
        return view.perform(source.getValues());
    }

    @Override
    public long getProfileStatus() {
        return source.getProfileStatus();
    }
}
