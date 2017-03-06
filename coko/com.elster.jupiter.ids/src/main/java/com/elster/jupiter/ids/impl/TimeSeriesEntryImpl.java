/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.TimeSeriesEntry;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class TimeSeriesEntryImpl implements TimeSeriesEntry {
    protected final TimeSeriesImpl timeSeries;
    protected final long timeStamp;
    protected final long version;
    protected final long recordTime;
    protected final Object[] values;

    TimeSeriesEntryImpl(TimeSeriesImpl timeSeries, ResultSet resultSet) throws SQLException {
        this.timeSeries = timeSeries;
        int offset = 2;
        this.timeStamp = resultSet.getLong(offset++);
        this.version = resultSet.getLong(offset++);
        this.recordTime = resultSet.getLong(offset++);
        List<? extends FieldSpec> fieldSpecs = timeSeries.getRecordSpec().getFieldSpecs();
        values = new Object[fieldSpecs.size()];
        for (int i = 0; i < fieldSpecs.size(); i++) {
            values[i] = ((FieldSpecImpl) fieldSpecs.get(i)).getValue(resultSet, offset++);
        }
    }

    TimeSeriesEntryImpl(TimeSeriesImpl timeSeries, Instant timeStamp, Object[] values) {
        this.timeSeries = timeSeries;
        this.timeStamp = timeStamp.toEpochMilli();
        this.version = 1;
        this.recordTime = 0;
        this.values = Arrays.copyOf(values, values.length);
    }

    private TimeSeriesEntryImpl(TimeSeriesEntryImpl source) {
        this.timeSeries = source.timeSeries;
        this.timeStamp = source.timeStamp;
        this.version = source.version;
        this.recordTime = source.recordTime;
        this.values = Arrays.copyOf(source.values, source.values.length);
    }

    @Override
    public TimeSeriesImpl getTimeSeries() {
        return timeSeries;
    }

    @Override
    public Instant getTimeStamp() {
        return Instant.ofEpochMilli(timeStamp);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getRecordDateTime() {
        return Instant.ofEpochMilli(recordTime);
    }

    @Override
    public Instant getInstant(int offset) {
        return (Instant) values[offset];
    }

    @Override
    public BigDecimal getBigDecimal(int offset) {
        return (BigDecimal) values[offset];
    }

    @Override
    public long getLong(int offset) {
        return (Long) values[offset];
    }

    @Override
    public String getString(int offset) {
        return (String) values[offset];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    @Override
    public String toString() {
        return "TimeSeries entry for " + getTimeStamp() + " recorded at " + getRecordDateTime();
    }

    boolean matches(TimeSeriesEntryImpl other) {
        return
                this.timeSeries.getId() == other.timeSeries.getId() &&
                        this.timeStamp == other.timeStamp &&
                        Arrays.equals(this.values, other.values);
    }

    void insert(PreparedStatement statement, long now) throws SQLException {
        int offset = 1;
        statement.setLong(offset++, getTimeSeries().getId());
        statement.setLong(offset++, timeStamp);
        statement.setLong(offset++, now);
        if (getTimeSeries().getVault().hasLocalTime()) {
            Calendar cal = getTimeSeries().getStartCalendar(getTimeStamp());
            statement.setTimestamp(offset++, new Timestamp(cal.getTime().getTime()), cal);
        }
        int i = 0;
        for (FieldSpec fieldSpec : getTimeSeries().getRecordSpec().getFieldSpecs()) {
            ((FieldSpecImpl) fieldSpec).bind(statement, offset++, values[i++]);
        }
    }

    void journal(PreparedStatement statement, long now, Principal principal) throws SQLException {
        int offset = 1;
        statement.setLong(offset++, now);
        statement.setString(offset++, principal.getName());
        statement.setLong(offset++, getTimeSeries().getId());
        statement.setLong(offset++, timeStamp);
    }

    void update(PreparedStatement statement, long now) throws SQLException {
        int offset = 1;
        statement.setLong(offset++, now);
        int i = 0;
        for (FieldSpec fieldSpec : getTimeSeries().getRecordSpec().getFieldSpecs()) {
            ((FieldSpecImpl) fieldSpec).bind(statement, offset++, values[i++]);
        }
        statement.setLong(offset++, getTimeSeries().getId());
        statement.setLong(offset++, timeStamp);
    }

    void set(int offset, Object value) {
        values[offset] = value;
    }

    TimeSeriesEntryImpl copy() {
        return new TimeSeriesEntryImpl(this);
    }

    long getTimeStampMs() {
        return timeStamp;
    }

    @Override
    public Optional<TimeSeriesEntry> getVersion(Instant at) {
        return getTimeSeries().getVault().getJournaledEntry(getTimeSeries(), getTimeStamp(), at);
    }
}
