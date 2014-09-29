package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
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
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataStorer storer;

    private final Map<Channel, Interval> scope = new HashMap<>();
    private final EventService eventService;

    public ReadingStorerImpl(IdsService idsService, EventService eventService, boolean overrules) {
        this.eventService = eventService;
        this.storer = idsService.createStorer(overrules);
	}
	
	@Override
	public void addIntervalReading(Channel channel, Date dateTime, ProfileStatus profileStatus, BigDecimal... values) {
		int i = 0;
		boolean useBulk = channel.getBulkQuantityReadingType().isPresent();
		Object[] entries = new Object[values.length + (useBulk ? 3 : 2)];
		entries[i++] = DEFAULTPROCESSSTATUS.getBits();
		entries[i++] = profileStatus.getBits();
		if (useBulk) {
			i++;
		}
		System.arraycopy(values, 0, entries, i, values.length);
		this.storer.add(channel.getTimeSeries(), dateTime, entries);
        addScope(channel, dateTime);
	}

	@Override
	public void addReading(Channel channel , Reading reading) {
		if (channel.isRegular()) {
			addIntervalReading(channel, reading.getTimeStamp(), ProfileStatus.of(),reading.getValue());
		} else {
			if (channel.hasMacroPeriod()) {
				Interval interval = reading.getTimePeriod();
				if (interval == null) {
					this.storer.add(channel.getTimeSeries(),reading.getTimeStamp(),DEFAULTPROCESSSTATUS.getBits(), reading.getValue(),reading.getText(),null,null);
				} else {
					this.storer.add(channel.getTimeSeries(),reading.getTimeStamp(),DEFAULTPROCESSSTATUS.getBits(), reading.getValue(),reading.getText(),interval.getStart(),interval.getEnd());
				}
			} else {
				this.storer.add(channel.getTimeSeries(),reading.getTimeStamp(),DEFAULTPROCESSSTATUS.getBits(), reading.getValue(),reading.getText());
			}
		}
        addScope(channel, reading.getTimeStamp());
    }

    @Override
	public void addReading(Channel channel, Date dateTime, BigDecimal value) {
		this.storer.add(channel.getTimeSeries(), dateTime, DEFAULTPROCESSSTATUS.getBits(),0L, value,null);
        addScope(channel, dateTime);
	}

	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from) {
		this.storer.add(channel.getTimeSeries(), dateTime, DEFAULTPROCESSSTATUS.getBits(), 0L, value, null,from);
        addScope(channel, dateTime);
	}
	
	public void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when) {
		this.storer.add(channel.getTimeSeries(),dateTime, DEFAULTPROCESSSTATUS.getBits(), 0L, value, null, from, when);
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
