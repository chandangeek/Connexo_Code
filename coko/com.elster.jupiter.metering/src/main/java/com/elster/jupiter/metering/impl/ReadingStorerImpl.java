package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReadingStorerImpl implements ReadingStorer {
    private static final ProcesStatus DEFAULTPROCESSSTATUS = ProcesStatus.of(); 
    private final TimeSeriesDataStorer storer;

    private final Map<Channel, Interval> scope = new HashMap<>();
    private final EventService eventService;

    public ReadingStorerImpl(IdsService idsService, EventService eventService, boolean overrules) {
        this.eventService = eventService;
        this.storer = idsService.createStorer(overrules);
	}
	
	@Override
	public void addIntervalReading(Channel channel, Date dateTime, ProfileStatus profileStatus, BigDecimal... values) {
		Object[] entries = new Object[values.length + 2];
		entries[0] = DEFAULTPROCESSSTATUS.getBits();
		entries[1] = profileStatus.getBits();
        System.arraycopy(values, 0, entries, 2, values.length);
		this.storer.add(channel.getTimeSeries(), dateTime, entries);
	}

	@Override 
	public void addReading(Channel channel , Reading reading) {
        this.storer.add(channel.getTimeSeries(),reading.getTimeStamp(),DEFAULTPROCESSSTATUS.getBits(), reading.getValue());
        addScope(channel, reading.getTimeStamp());
    }

    @Override
	public void addReading(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(), dateTime, DEFAULTPROCESSSTATUS.getBits(),0L, value);
        addScope(channel, dateTime);
	}

	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime, DEFAULTPROCESSSTATUS.getBits(), 0L, value, from);
        addScope(channel, dateTime);
	}
	
	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime, DEFAULTPROCESSSTATUS.getBits(), 0L, value, from, when);
        addScope(channel, dateTime);
	}

	@Override
	public void execute() {
		storer.execute();
        eventService.postEvent(EventType.READINGS_CREATED.topic(), this);
	}
	
	@Override
	public boolean overrules() {
		return storer.overrules();
	}

    @Override
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
