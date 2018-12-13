/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.TimeSeriesJournalEntry;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.JournaledRegisterReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

public class JournaledRegisterReadingRecordImpl extends ReadingRecordImpl implements JournaledRegisterReadingRecord {

    JournaledRegisterReadingRecordImpl(ChannelContract channel, TimeSeriesEntry entry) {
        super(channel, entry);
    }

    @Override
    public JournaledRegisterReadingRecord filter(ReadingType readingType) {
        return new FilteredJournaledRegisterReadingRecord(this, getIndex(readingType));
    }


    @Override
    public Instant getJournalTime() {
        return getEntry() instanceof TimeSeriesJournalEntry ? ((TimeSeriesJournalEntry) getEntry()).getJournalTime() : Instant.EPOCH;
    }

    @Override
    public String getUserName() {
        return getEntry() instanceof TimeSeriesJournalEntry && ((TimeSeriesJournalEntry) getEntry()).getUserName() != null ?
                ((TimeSeriesJournalEntry) getEntry()).getUserName() : "";
    }

    @Override
    public Channel getChannel() {
        return super.getChannel();
    }
}
