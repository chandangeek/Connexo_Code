package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.BaseReading;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReadingStorerImpl implements ReadingStorer {
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataStorer storer;

    private final Map<Channel, Range<Instant>> scope = new HashMap<>();
    private final EventService eventService;

    public ReadingStorerImpl(IdsService idsService, EventService eventService, boolean overrules) {
        this.eventService = eventService;
        this.storer = idsService.createStorer(overrules);
    }

    @Override
    public void addReading(Channel channel, BaseReading reading) {
        addReading(channel, reading, DEFAULTPROCESSSTATUS);
    }

    @Override
    public void addReading(Channel channel, BaseReading reading, ProcessStatus status) {
        Object[] values = ((ChannelContract) channel).toArray(reading, status);
        ((ChannelContract) channel).validateValues(reading, values);
        this.storer.add(((ChannelContract) channel).getTimeSeries(), reading.getTimeStamp(), values);
        addScope(channel, reading.getTimeStamp());
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
    public Map<Channel, Range<Instant>> getScope() {
        return Collections.unmodifiableMap(scope);
    }

    private void addScope(Channel channel, Instant timestamp) {
        scope.merge(channel, Range.singleton(timestamp), Range::span);
    }

    @Override
    public boolean processed(Channel channel, Instant instant) {
        return storer.processed(((ChannelContract) channel).getTimeSeries(), instant);
    }
}
