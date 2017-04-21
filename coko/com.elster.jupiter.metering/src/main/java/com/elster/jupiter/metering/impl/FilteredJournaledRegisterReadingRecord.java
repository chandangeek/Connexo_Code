/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.JournaledRegisterReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

public class FilteredJournaledRegisterReadingRecord extends FilteredReadingRecord implements JournaledRegisterReadingRecord {

    private final JournaledRegisterReadingRecord journaledRegisterReadingRecord;

    FilteredJournaledRegisterReadingRecord(JournaledRegisterReadingRecordImpl journaledReadingRecord, int... indices) {
        super(journaledReadingRecord, indices);
        this.journaledRegisterReadingRecord = journaledReadingRecord;
    }

    @Override
    public JournaledRegisterReadingRecord filter(ReadingType readingType) {
        return journaledRegisterReadingRecord.filter(readingType);
    }

    @Override
    public Instant getJournalTime() {
        return journaledRegisterReadingRecord.getJournalTime();
    }

    @Override
    public String getUserName() {
        return journaledRegisterReadingRecord.getUserName();
    }

    @Override
    public Channel getChannel() {
        return journaledRegisterReadingRecord.getChannel();
    }
}
