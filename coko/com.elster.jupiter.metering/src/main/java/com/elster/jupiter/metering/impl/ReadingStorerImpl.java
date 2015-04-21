package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.util.Pair;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class ReadingStorerImpl implements ReadingStorer {
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataStorer storer;

    private final Map<CimChannel, Range<Instant>> scope = new HashMap<>();
    private final EventService eventService;
    private final Map<Pair<Channel, Instant>, Object[]> consolidatedValues = new HashMap<>();
    private final Map<Pair<Channel, Instant>, BaseReading> readings = new HashMap<>();
    private final StorerProcess storerProcess;

    private ReadingStorerImpl(IdsService idsService, EventService eventService, UpdateBehaviour updateBehaviour, StorerProcess storerProcess) {
        this.eventService = eventService;
        this.storer = updateBehaviour.createTimeSeriesStorer(idsService);
        this.storerProcess = storerProcess;
    }

    static ReadingStorer createNonOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.INSERT_ONLY, StorerProcess.DEFAULT);
    }

    static ReadingStorer createOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.OVERRULE, StorerProcess.DEFAULT);
    }

    static ReadingStorer createUpdatingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.UPDATE, StorerProcess.DEFAULT);
    }

    static ReadingStorer createUpdatingStorer(IdsService idsService, EventService eventService, StorerProcess storerProcess) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.UPDATE, storerProcess);
    }

    private interface UpdateBehaviour {
        TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService);
    }

    private enum Behaviours implements UpdateBehaviour {
        INSERT_ONLY {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createNonOverrulingStorer();
            }
        },

        OVERRULE {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createOverrulingStorer();
            }
        },

        UPDATE {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createUpdatingStorer();
            }
        };
    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading) {
        addReading(channel, reading, DEFAULTPROCESSSTATUS);
    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading, ProcessStatus status) {
        Pair<Channel, Instant> key = Pair.of(channel.getChannel(), reading.getTimeStamp());
        int offset = channel.isRegular() ? 2 : 1;

        ChannelContract channelContract = (ChannelContract) channel.getChannel();
        List<? extends FieldSpec> fieldSpecs = channelContract.getTimeSeries().getRecordSpec().getFieldSpecs();

        Object[] values = consolidatedValues.computeIfAbsent(key, k -> {
            Object[] newValues = new Object[fieldSpecs.size()];
            IntStream.range(0, newValues.length).forEach(i -> newValues[i] = storer.doNotUpdateMarker());
            return newValues;
        });
        Object[] valuesToAdd = channelContract.toArray(reading, channel.getReadingType(), status);

        IntStream.range(offset, values.length)
                .filter(i -> valuesToAdd[i] != null)
                .forEach(i -> {
                    values[i] = valuesToAdd[i];
                });

        ProcessStatus processStatus;
        if (values[0] != null && !storer.doNotUpdateMarker().equals(values[0])) {
            processStatus = status.or(new ProcessStatus((Long) values[0]));
        } else {
            processStatus = status;
        }
        values[0] = processStatus.getBits();
        if (reading instanceof IntervalReading) {
            IntervalReading intervalReading = (IntervalReading) reading;
            long bits = (values[1] == null) ? 0L : (long) values[1];
            bits |= intervalReading.getProfileStatus().getBits();
            values[1] = bits;
        }
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
                    storer.add(channel.getTimeSeries(), timestamp, values);
                });
        storer.execute();
        eventService.postEvent(EventType.READINGS_CREATED.topic(), this);
    }

    @Override
    public boolean overrules() {
        return storer.overrules();
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
        return storer.processed(((ChannelContract) channel).getTimeSeries(), instant);
    }

    @Override
    public StorerProcess getStorerProcess() {
        return storerProcess;
    }
}
