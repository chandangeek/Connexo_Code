/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.TimeSeriesJournalEntry;
import com.elster.jupiter.metering.IntervalReadingJournalRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

public class IntervalReadingJournalRecordImpl extends BaseReadingRecordImpl implements IntervalReadingJournalRecord {
    private final TimeSeriesJournalEntry entry;

    IntervalReadingJournalRecordImpl(ChannelContract channel, TimeSeriesJournalEntry entry) {
        super(channel, entry);
        this.entry = entry;
    }

    @Override
    int getReadingTypeOffset() {
        return 2;
    }

    @Override
    public IntervalReadingJournalRecord filter(ReadingType readingType) {
        return new FilteredIntervalReadingJournalRecord(this, getIndex(readingType));
    }

    @Override
    public Instant getJournalTime() {
        return entry.getJournalTime();
    }

    @Override
    public String getUserName() {
        return entry.getUserName();
    }

    @Override
    public long getVersion() {
        return entry.getVersion();
    }


    @Override
    public Boolean getActive() {
        return entry.getActive();
    }

    @Override
    public IntervalReadingRecord getIntervalReadingRecord() {
        IntervalReadingRecordImpl intervalReadingRecordImpl = new IntervalReadingRecordImpl((ChannelContract) this.getChannel(), (TimeSeriesEntry) entry);
        FilteredIntervalReadingRecord filteredIntervalReadingRecord = new FilteredIntervalReadingRecord(intervalReadingRecordImpl, getIndex(getReadingType(1)));
        return filteredIntervalReadingRecord;
    }
}
