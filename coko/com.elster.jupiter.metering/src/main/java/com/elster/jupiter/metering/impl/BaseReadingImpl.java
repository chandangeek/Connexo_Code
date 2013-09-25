package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReading;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.text.MessageFormat;
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
        ImmutableList.Builder<BigDecimal> builder = ImmutableList.builder();
        for (int i = getReadingTypeOffset(); i < entry.size(); i++) {
            builder.add(entry.getBigDecimal(i));
        }
        return builder.build();
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
            }
            i++;
        }
        throw new IllegalArgumentException(MessageFormat.format("ReadingType {0} does not occur on this channel", readingType.getMRID()));
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
