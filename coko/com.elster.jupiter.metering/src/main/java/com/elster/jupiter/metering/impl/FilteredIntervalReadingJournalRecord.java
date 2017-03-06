/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.IntervalReadingJournalRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

/**
 * Decorates an IntervalReading by selecting only certain values in a possibly different order.
 */
public class FilteredIntervalReadingJournalRecord extends FilteredBaseReadingRecord implements IntervalReadingJournalRecord {

    private final IntervalReadingJournalRecord source;

    FilteredIntervalReadingJournalRecord(IntervalReadingJournalRecordImpl source, int... indices) {
        super(source, indices);
        this.source = source;
    }

    @Override
    public Instant getJournalTime() {
        return source.getJournalTime();
    }

    @Override
    public String getUserName() {
        return source.getUserName();
    }

    @Override
    public Boolean getActive() {
        return source.getActive();
    }

    @Override
    public IntervalReadingRecord getIntervalReadingRecord() {
        return source.getIntervalReadingRecord();
    }

    @Override
    public long getVersion() {
        return source.getVersion();
    }

    @Override
    public IntervalReadingJournalRecord filter(ReadingType readingType) {
        return source.filter(readingType);
    }
}
