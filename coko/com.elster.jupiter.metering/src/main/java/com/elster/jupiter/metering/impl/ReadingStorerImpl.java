package com.elster.jupiter.metering.impl;

import java.math.BigDecimal;
import java.util.Date;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.Bus;

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
		for (int i = 0 ; i < values.length ; i++) {
			entries[i+2] = values[i];
		}
		this.storer.add(channel.getTimeSeries(), dateTime,entries);
	}

	@Override
	public void add(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(),dateTime,0L,0L,value);
	}

	public void add(Channel channel, Date dateTime, BigDecimal value,Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime,0L,0L,value,from);		
	}
	
	public void add(Channel channel, Date dateTime, BigDecimal value,Date from ,Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime,0L,0L,value,from,when);
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
