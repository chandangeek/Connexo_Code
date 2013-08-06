package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReading;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class BaseReadingImpl implements BaseReading {
	private final Channel channel;
	private final TimeSeriesEntry entry;
	
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
    public List<BigDecimal> getValues() {
        List<BigDecimal> result = new ArrayList<>(entry.size() - getReadingTypeOffset());
        for (int i = getReadingTypeOffset(); i < entry.size(); i++) {
            result.add(entry.getBigDecimal(i));
        }
        return result;
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
