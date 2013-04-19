package com.elster.jupiter.metering.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.*;


abstract public class BaseReadingImpl implements BaseReading {
	final private Channel channel;
	final private TimeSeriesEntry entry; 
	
	BaseReadingImpl(Channel channel , TimeSeriesEntry entry) {
		this.channel = channel;
		this.entry = entry;
	}

	TimeSeriesEntry getEntry() {
		return entry;
	}
	
	Channel getChannel() {
		return channel;
	}
	
	@Override
	public Date getTimeStamp() {
		return entry.getTimeStamp();
	}

	@Override
	public Date getReportedDateTime() {
		return entry.getRecordDateTime();
	}

	abstract int getReadingTypeOffset();
	
	@Override
	public BigDecimal getValue() {
		return getValue(0);
	}

	@Override
	public BigDecimal getValue(int offset) {
		return entry.getBigDecimal(getReadingTypeOffset() + offset);
	}

	@Override
	public BigDecimal getValue(ReadingType readingType) {
		int i = 0;
		for (ReadingType each : channel.getReadingTypes()) {
			if (each.equals(readingType)) {
				return getValue(i);
			} else {
				i++;
			}
		}
		return null;
	}

	@Override
	public ReadingType getReadingType() {
		return getReadingType(0);
	}

	@Override
	public ReadingType getReadingType(int offset) {
		return channel.getReadingTypes().get(offset);
	}

	@Override
	public List<ReadingType> getReadingTypes() {
		return channel.getReadingTypes();
	}

	@Override
	public long getProcessingFlags() {
		return entry.getLong(0);
	}

}
