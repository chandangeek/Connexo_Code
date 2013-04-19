package com.elster.jupiter.ids.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.*;
import java.sql.*;

import com.elster.jupiter.ids.*;

public class TimeSeriesEntryImpl implements TimeSeriesEntry {
	final private TimeSeries timeSeries;
	final private long timeStamp;
	final private long version;
	final private long recordTime;		
	final private Object[] values;
	
	TimeSeriesEntryImpl(TimeSeries timeSeries, ResultSet resultSet) throws SQLException {		
		this.timeSeries = timeSeries;
		int offset = 2;
		this.timeStamp = resultSet.getLong(offset++);
		this.version = resultSet.getLong(offset++);
		this.recordTime = resultSet.getLong(offset++);
		List<FieldSpec> fieldSpecs = timeSeries.getRecordSpec().getFieldSpecs();
		values = new Object[fieldSpecs.size()];
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {			
			values[i] = ((FieldSpecImpl) fieldSpecs.get(i)).getValue(resultSet , offset++);
		}			
	}

	TimeSeriesEntryImpl(TimeSeries timeSeries , Date timeStamp , Object[] values) {
		this.timeSeries = timeSeries;
		this.timeStamp = timeStamp.getTime();
		this.version = 0;
		this.recordTime = 0;
		this.values = values;
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
	
}
