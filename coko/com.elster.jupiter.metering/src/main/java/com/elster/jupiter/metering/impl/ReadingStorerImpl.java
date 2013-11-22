package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReadingStorerImpl implements ReadingStorer {
    private static final long PROCESSING_FLAGS_DEFAULT = 0L;
    private final TimeSeriesDataStorer storer;

    private final Map<Channel, Interval> scope = new HashMap<>();

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
        addScope(channel, reading.getTimeStamp());
    }

    @Override
	public void addReading(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(), dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value);
        addScope(channel, dateTime);
	}

	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value, from);
        addScope(channel, dateTime);
	}
	
	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime, PROCESSING_FLAGS_DEFAULT, 0L, value, from, when);
        addScope(channel, dateTime);
	}

	@Override
	public void execute() {
		storer.execute();
        Bus.getEventService().postEvent(EventType.READINGS_CREATED.topic(), this);
	}
	
	@Override
	public boolean overrules() {
		return storer.overrules();
	}

    public Map<Channel, Interval> getScope() {
        return Collections.unmodifiableMap(scope);
    }

    private void addScope(Channel channel, Date timestamp) {
        if (!scope.containsKey(channel)) {
            scope.put(channel, new Interval(timestamp, timestamp));
            return;
        }
        scope.put(channel, scope.get(channel).spanToInclude(timestamp));
    }
}
