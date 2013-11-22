package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.Reading;

import java.math.BigDecimal;
import java.util.Date;

public class ReadingStorerImpl implements ReadingStorer {

    private static final long PROCESSING_FLAGS_DEFAULT = 0L;
    private final TimeSeriesDataStorer storer;

	public ReadingStorerImpl(boolean overrules) {
		this.storer = Bus.getIdsService().createStorer(overrules);
	}
	
	@Override
	public void addIntervalReading(Channel channel, Date dateTime, long profileStatus, BigDecimal... values) {
		Object[] entries = new Object[values.length + 2];
		entries[0] = PROCESSING_FLAGS_DEFAULT;
		entries[1] = profileStatus;
        System.arraycopy(values, 0, entries, 2, values.length);
		this.storer.add(channel.getTimeSeries(), dateTime, entries);
	}

	@Override 
	public void addReading(Channel channel , Reading reading) {
		addReading(channel,reading.getTimeStamp(),reading.getValue());
	}
	
	@Override
	public void addReading(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(), dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value);
	}

	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value, from);
	}
	
	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value, from, when);
	}
	
	@Override
	public boolean overrules() {
		return storer.overrules();
	}

	@Override
	public void execute() {
		storer.execute();
        Bus.getEventService().postEvent(EventType.READINGS_CREATED.topic(), this);
	}

}
