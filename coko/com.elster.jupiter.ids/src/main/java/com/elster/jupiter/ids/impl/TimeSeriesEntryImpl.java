package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TimeSeriesEntryImpl implements TimeSeriesEntry  {
	private final TimeSeriesImpl timeSeries;
	private final long timeStamp;
	private final long version;
	private final long recordTime;
	private Object[] values;
	
	TimeSeriesEntryImpl(TimeSeriesImpl timeSeries, ResultSet resultSet) throws SQLException {		
		this.timeSeries = timeSeries;
		int offset = 2;
		this.timeStamp = resultSet.getLong(offset++);
		this.version = resultSet.getLong(offset++);
		this.recordTime = resultSet.getLong(offset++);
		List<? extends FieldSpec> fieldSpecs = timeSeries.getRecordSpec().getFieldSpecs();
		values = new Object[fieldSpecs.size()];
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {			
			values[i] = ((FieldSpecImpl) fieldSpecs.get(i)).getValue(resultSet , offset++);
		}			
	}

	TimeSeriesEntryImpl(TimeSeriesImpl timeSeries , Date timeStamp , Object[] values) {
		this.timeSeries = timeSeries;
		this.timeStamp = timeStamp.getTime();
		this.version = 1;
		this.recordTime = 0;
		int derivedFieldCount = ((RecordSpecImpl) timeSeries.getRecordSpec()).derivedFieldCount();
		this.values = new Object[values.length + derivedFieldCount];
		Iterator<? extends FieldSpec> it = timeSeries.getRecordSpec().getFieldSpecs().iterator();
		int offset = 0;
		for (int i = 0 ; i < values.length ; i++) {
			FieldSpec fieldSpec = it.next();
			while (fieldSpec.isDerived()) {
				offset++;
				fieldSpec = it.next();
			}
			this.values[i+offset] = values[i];
		}
	}
	
	private TimeSeriesEntryImpl(TimeSeriesEntryImpl source) {
		this.timeSeries = source.timeSeries;
		this.timeStamp = source.timeStamp;
		this.version = source.version;
		this.recordTime = source.recordTime;
		this.values = Arrays.copyOf(source.values, source.values.length);
	}
	
	@Override 
	public TimeSeries getTimeSeries() {
		return timeSeries;
	}
	
	@Override
	public Date getTimeStamp() {
		return new Date(timeStamp);
	}

	@Override
	public long getVersion() {
		return version;
	}
	
	@Override
	public Date getRecordDateTime() {
		return new Date(recordTime);
	}

	@Override
	public Date getDate(int offset) {
		return (Date) values[offset];
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
    public int size() {
        return values.length;
    }

    @Override
	public String toString() {
		return "TimeSeries entry for " + getTimeStamp() + " recorded at " + getRecordDateTime();
	}
	
	boolean matches(TimeSeriesEntryImpl other) {
		return
			this.timeSeries.getId() == other.timeSeries.getId() &&
			this.timeStamp == other.timeStamp &&
			Arrays.equals(this.values,other.values);				
	}
	
	void insert(PreparedStatement statement, long now) throws SQLException {
		int offset = 1;
		statement.setLong(offset++, getTimeSeries().getId());
		statement.setLong(offset++, timeStamp);
		statement.setLong(offset++, now);
		if (getTimeSeries().getVault().hasLocalTime()) {
			Calendar cal = ((TimeSeriesImpl) getTimeSeries()).getStartCalendar(getTimeStamp());
			statement.setTimestamp(offset++, new Timestamp(cal.getTime().getTime()),cal);
		}
		int i = 0;
		for (FieldSpec fieldSpec : getTimeSeries().getRecordSpec().getFieldSpecs()) {
			((FieldSpecImpl) fieldSpec).bind(statement, offset++ ,  values[i++]);				
		}
	}
	
	void journal(PreparedStatement statement, long now) throws SQLException {
		int offset = 1;
		statement.setLong(offset++, now);
		statement.setLong(offset++, getTimeSeries().getId());
		statement.setLong(offset++, timeStamp);		
	}
	
	void update(PreparedStatement statement, long now) throws SQLException {
		int offset = 1;
		statement.setLong(offset++, now);			
		int i = 0;
		for (FieldSpec fieldSpec : getTimeSeries().getRecordSpec().getFieldSpecs()) {
			((FieldSpecImpl) fieldSpec).bind(statement, offset++ ,  values[i++]);				
		}
		statement.setLong(offset++, getTimeSeries().getId());
		statement.setLong(offset++, timeStamp);		
	}
	
	void set(int offset,Object value) {
		values[offset] = value;
	}
	
	TimeSeriesEntryImpl copy() {
		return new TimeSeriesEntryImpl(this);
	}
	
	long getTimeStampMs() {
		return timeStamp;
	}
	
}
