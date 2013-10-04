package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingStorer;

import java.math.BigDecimal;
import java.util.Date;

public class ReadingStorerImpl implements ReadingStorer {
	
	private final TimeSeriesDataStorer storer;

	public ReadingStorerImpl(boolean overrules) {
		this.storer = Bus.getIdsService().createStorer(overrules);
	}
	
	@Override
	public void add(Channel channel, Date dateTime, long profileStatus, BigDecimal... values) {
		Object[] entries = new Object[values.length + 2];
		entries[0] = 0L;
		entries[1] = profileStatus;
        System.arraycopy(values, 0, entries, 2, values.length);
		this.storer.add(channel.getTimeSeries(), dateTime, entries);
	}

	@Override
	public void add(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(), dateTime, 0L, 0L, value);
	}

	public void add(Channel channel, Date dateTime, BigDecimal value,Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime, 0L, 0L, value, from);
	}
	
	public void add(Channel channel, Date dateTime, BigDecimal value,Date from ,Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime, 0L, 0L, value, from, when);
	}
	
	@Override
	public boolean overrules() {
		return storer.overrules();
	}

	@Override
	public void execute() {
		storer.execute();
	}

}
