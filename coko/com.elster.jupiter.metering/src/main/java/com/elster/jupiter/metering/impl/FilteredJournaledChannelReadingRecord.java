/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.JournaledChannelReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

public class FilteredJournaledChannelReadingRecord extends FilteredBaseReadingRecord implements JournaledChannelReadingRecord {

    private final JournaledChannelReadingRecord journaledChannelReadingRecord;

    FilteredJournaledChannelReadingRecord(JournaledChannelReadingRecordImpl journaledReadingRecord, int... indices) {
        super(journaledReadingRecord, indices);
        this.journaledChannelReadingRecord = journaledReadingRecord;
    }

    @Override
    public JournaledChannelReadingRecord filter(ReadingType readingType) {
        return journaledChannelReadingRecord.filter(readingType);
    }

    @Override
    public Instant getJournalTime() {
        return journaledChannelReadingRecord.getJournalTime();
    }

    @Override
    public String getUserName() {
        return journaledChannelReadingRecord.getUserName();
    }

    @Override
    public Channel getChannel() {
        return journaledChannelReadingRecord.getChannel();
    }
}
