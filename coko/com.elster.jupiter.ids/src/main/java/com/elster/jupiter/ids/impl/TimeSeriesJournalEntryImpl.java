/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.TimeSeriesJournalEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class TimeSeriesJournalEntryImpl extends TimeSeriesEntryImpl implements TimeSeriesJournalEntry {
    private final long journalTime;
    private final String userName;
    private final boolean isActive;


    TimeSeriesJournalEntryImpl(TimeSeriesImpl timeSeries, ResultSet resultSet) throws SQLException {
        super(timeSeries, resultSet);

        this.journalTime = resultSet.getLong("JOURNALTIME");
        this.userName = resultSet.getString("USERNAME");
        this.isActive = resultSet.getBoolean("ISACTIVE");
    }

    TimeSeriesJournalEntryImpl(TimeSeriesImpl timeSeries, Instant timeStamp, Object[] values) {
        super(timeSeries, timeStamp, values);
        this.journalTime = timeStamp.toEpochMilli();
        this.userName = "";
        this.isActive = false;
    }

    @Override
    public Instant getJournalTime() {
        return Instant.ofEpochMilli(journalTime);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public Boolean getActive() {
        return isActive;
    }


}
