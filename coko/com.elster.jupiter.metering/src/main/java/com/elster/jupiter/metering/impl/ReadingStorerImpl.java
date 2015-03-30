package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataWriter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.Pair;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReadingStorerImpl implements ReadingStorer {
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataWriter writer;

    private final Map<CimChannel, Range<Instant>> scope = new HashMap<>();
    private final EventService eventService;
    private final Map<Pair<Channel, Instant>, Object[]> consolidatedValues = new HashMap<>();
    private final Map<Pair<Channel, Instant>, BaseReading> readings = new HashMap<>();

    public ReadingStorerImpl(IdsService idsService, EventService eventService, boolean overrules) {
        this.eventService = eventService;
        this.writer = idsService.createWriter(overrules);
    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading) {
        addReading(channel, reading, DEFAULTPROCESSSTATUS);
    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading, ProcessStatus status) {
        Pair<Channel, Instant> key = Pair.of(channel.getChannel(), reading.getTimeStamp());
        int offset = channel.isRegular() ? 2 : 1;

        Object[] values = consolidatedValues.computeIfAbsent(key, k -> new Object[offset + k.getFirst().getReadingTypes().size()]);
        values[offset + channel.getChannel().getReadingTypes().indexOf(channel.getReadingType())] = reading.getValue();
        ProcessStatus processStatus;
        if (values[0] != null) {
            processStatus = status.or(new ProcessStatus((Long) values[0]));
        } else {
            processStatus = status;
        }
        values[0] = processStatus.getBits();
        addScope(channel, reading.getTimeStamp());
    }

    @Override
    public void execute() {
        consolidatedValues.entrySet().stream()
                .forEach(entry -> {
                    ChannelContract channel = (ChannelContract) entry.getKey().getFirst();
                    Instant timestamp = entry.getKey().getLast();
                    Object[] values = entry.getValue();
                    channel.validateValues(readings.get(entry.getKey()), values);
                    writer.add(channel.getTimeSeries(), timestamp, values);
                });
        writer.execute();
        eventService.postEvent(EventType.READINGS_CREATED.topic(), this);
    }

    @Override
    public boolean overrules() {
        return writer.overrules();
    }

    @Override
    public Map<CimChannel, Range<Instant>> getScope() {
        return Collections.unmodifiableMap(scope);
    }

    private void addScope(CimChannel channel, Instant timestamp) {
        scope.merge(channel, Range.singleton(timestamp), Range::span);
    }

    @Override
    public boolean processed(Channel channel, Instant instant) {
        return writer.processed(((ChannelContract) channel).getTimeSeries(), instant);
    }
}
