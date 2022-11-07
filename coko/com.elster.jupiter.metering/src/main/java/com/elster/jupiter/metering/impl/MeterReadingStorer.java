/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import com.google.common.collect.Range;

import javax.inject.Provider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeterReadingStorer {

    private final ReadingStorer readingStorer;
    private final MeterReadingFacade facade;
    private final Meter meter;
    private final EventService eventService;

    private static final Logger logger = Logger.getLogger(MeterReadingStorer.class.getName());
    private final MeteringService meteringService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    private final Map<Channel, Map<Instant, BaseReading>> channelReadings = new HashMap<>();
    private final Map<String, ReadingType> readingTypes = new HashMap<>();

    MeterReadingStorer(DataModel dataModel, MeteringService meteringService, Meter meter,
                       MeterReading meterReading, Thesaurus thesaurus, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.meter = meter;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.facade = new MeterReadingFacade(meterReading);
        this.readingStorer = this.meteringService.createOverrulingStorer();
        this.deviceEventFactory = deviceEventFactory;
    }

    void store(QualityCodeSystem system) {
        this.store(system, null);
    }

    void store(QualityCodeSystem system, Instant readingDate) {
        getReadingTypes();
        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        if (meterActivations.isEmpty()) {
            createDefaultMeterActivation();
        }
        storeReadings(facade.getMeterReading().getReadings());
        storeIntervalBlocks(facade.getMeterReading().getIntervalBlocks());
        removeOldReadingQualities();
        storeReadingQualities();
        storeEvents(facade.getMeterReading().getEvents(), readingDate);
        readingStorer.execute(system);
        facade.getRange()
                .ifPresent(range -> eventService.postEvent(EventType.METERREADING_CREATED.topic(), new EventSource(meter
                        .getId(), range.lowerEndpoint().toEpochMilli(), range.upperEndpoint()
                        .toEpochMilli())));
    }

    private void getReadingTypes() {
        facade.readingTypeCodes().forEach(this::findOrCreateReadingType);
    }

    private void removeOldReadingQualities() {
        Optional<Range<Instant>> range = facade.getRange();
        if (range.isPresent()) {
            List<ReadingQualityRecord> readingQualitiesForRemoval = meter.getReadingQualities(range.get())
                    .stream()
                    .filter(this::isRelevant)
                    .collect(Collectors.<ReadingQualityRecord>toList());
            ReadingQualityRecordImpl.deleteAll(dataModel, readingQualitiesForRemoval);
        }
    }

    private boolean isRelevant(ReadingQualityRecord readingQuality) {
        Map<Instant, BaseReading> readingMap = channelReadings.get(readingQuality.getChannel());
        return readingMap != null && readingMap.containsKey(readingQuality.getReadingTimestamp());
    }

    public static class EventSource {

        private long start;
        private long end;
        private long meterId;

        public EventSource(long meterId, long start, long end) {
            this.end = end;
            this.meterId = meterId;
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public long getMeterId() {
            return meterId;
        }

        public void setMeterId(long meterId) {
            this.meterId = meterId;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }
    }

    private void storeEvents(List<EndDeviceEvent> events, Instant readingDate) {
        List<EndDeviceEventRecord> toCreate = new ArrayList<>(events.size());
        List<EndDeviceEventRecord> toUpdate = new ArrayList<>();
        for (EndDeviceEvent sourceEvent : events) {
            String eventCode = sourceEvent.getEventTypeCode();
            Optional<EndDeviceEventType> found = dataModel.mapper(EndDeviceEventType.class).getOptional(eventCode);
            if (!found.isPresent()){
                try {
                    logger.info("Event code "+eventCode+" is not yet known to the system, we'll add it now");
                    meteringService.createEndDeviceEventType(eventCode);
                } catch (Exception ex){
                    logger.log(Level.SEVERE, "Cannot add new event code: "+eventCode+" "+ex.getMessage(), ex);
                }
                found = dataModel.mapper(EndDeviceEventType.class).getOptional(eventCode);
            }
            if (found.isPresent()) {
                Optional<EndDeviceEventRecord> existing = getEventMapper().getOptional(meter.getId(), sourceEvent.getEventTypeCode(), sourceEvent.getCreatedDateTime(), sourceEvent.getLogBookId());
                if (existing.isPresent()) {
                    if (existing.get().updateProperties(sourceEvent.getEventData())) {
                        toUpdate.add(existing.get());
                    }
                } else {
                    toCreate.add(createEventRecord(found.get(), sourceEvent, readingDate));
                }
            } else {
                EndDeviceEventType endDeviceEventType = meteringService.createEndDeviceEventType(sourceEvent.getEventTypeCode());
                toCreate.add(createEventRecord(endDeviceEventType, sourceEvent, readingDate));
                PrivateMessageSeeds.UNEXPECTED_METER_EVENT_LOGGED.log(logger, thesaurus, sourceEvent.getEventTypeCode(), meter.getName());
            }
        }
        getEventMapper().persist(toCreate);
        for (EndDeviceEventRecord endDeviceEventRecord : toCreate) {
            eventService.postEvent(EventType.END_DEVICE_EVENT_CREATED.topic(), endDeviceEventRecord);
        }
        getEventMapper().update(toUpdate);
        for (EndDeviceEventRecord endDeviceEventRecord : toUpdate) {
            eventService.postEvent(EventType.END_DEVICE_EVENT_UPDATED.topic(), endDeviceEventRecord);
        }
    }

    private EndDeviceEventRecord createEventRecord(EndDeviceEventType found, EndDeviceEvent sourceEvent, Instant readingDate) {
        EndDeviceEventRecordImpl eventRecord = deviceEventFactory.get().init(meter, found, sourceEvent.getCreatedDateTime(), sourceEvent.getLogBookId());
        eventRecord.updateProperties(sourceEvent.getEventData());
        eventRecord.setmRID(sourceEvent.getMRID());
        eventRecord.setReason(sourceEvent.getReason());
        eventRecord.setSeverity(sourceEvent.getSeverity());
        eventRecord.setStatus(sourceEvent.getStatus());
        eventRecord.setIssuerID(sourceEvent.getIssuerID());
        eventRecord.setIssuerTrackingID(sourceEvent.getIssuerTrackingID());
        eventRecord.setName(sourceEvent.getName());
        eventRecord.setDescription(sourceEvent.getDescription());
        eventRecord.setAliasName(sourceEvent.getAliasName());
        eventRecord.setLogBookPosition(sourceEvent.getLogBookPosition());
        eventRecord.setDeviceEventType(sourceEvent.getType());
        eventRecord.setReadingDateTime(readingDate);
        return eventRecord;
    }

    private DataMapper<EndDeviceEventRecord> getEventMapper() {
        return dataModel.mapper(EndDeviceEventRecord.class);
    }

    private void createDefaultMeterActivation() {
        facade.getRange().ifPresent(range -> meter.activate(range.lowerEndpoint()));
    }

    private void storeReadings(List<Reading> readings) {
        for (Reading reading : readings) {
            store(reading);
        }
    }

    private void store(Reading reading) {
        boolean stored = false;
        for (ChannelsContainer channelsContainer : meter.getChannelsContainers()) {
            logger.log(Level.INFO, "Check if  " + reading.getReadingTypeCode() + " with reading date " + reading.getTimeStamp() + " included in " + channelsContainer.getInterval().toClosedRange());
            if (channelsContainer.getInterval().toClosedRange().contains(reading.getTimeStamp())) {
                stored = true;
                store(reading, channelsContainer);
            }
        }
        logger.log(Level.INFO, "Reading " + reading.getReadingTypeCode() + " with value/text " + reading.getValue() + "/" + reading.getText() + " stored: " + stored);
    }

    private void store(Reading reading, ChannelsContainer channelsContainer) {
        Channel channel = findOrCreateChannel(reading, channelsContainer);
        if (channel != null) {
            ReadingType readingType = channel.getReadingTypes().stream()
                    .filter(type -> type.getMRID().equals(reading.getReadingTypeCode()))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
            readingStorer.addReading(channel.getCimChannel(readingType).get(), reading);
            addedReading(channel, reading);
        }
    }

    private void storeIntervalBlocks(List<IntervalBlock> blocks) {
        for (IntervalBlock block : blocks) {
            store(block);
        }
    }

    private void store(IntervalBlock block) {
        String readingTypeCode = block.getReadingTypeCode();
        for (IntervalReading each : block.getIntervals()) {
            store(each, readingTypeCode);
        }
    }

    private void store(IntervalReading reading, String readingTypeCode) {
        ReadingType readingType = Objects.requireNonNull(readingTypes.get(readingTypeCode));
        if (!readingType.isRegular()) {
            throw new IllegalArgumentException(readingTypeCode + " is not valid for interval readings ");
        }
        Channel channel = findOrCreateChannel(reading, readingType);
        if (channel != null) {
            readingStorer.addReading(channel.getCimChannel(readingType).get(), reading);
            addedReading(channel, reading);
        }
    }

    private void findOrCreateReadingType(String code) {
        readingTypes.put(code, doFindOrCreateReadingType(code));
    }

    private ReadingType doFindOrCreateReadingType(String code) {
        Optional<ReadingType> readingTypeHolder = dataModel.mapper(ReadingType.class).getOptional(code);
        if (readingTypeHolder.isPresent()) {
            return readingTypeHolder.get();
        } else {
            try {
                ReadingType readingType = meteringService.createReadingType(code, "");
                PrivateMessageSeeds.READINGTYPE_ADDED.log(logger, thesaurus, code, meter.getName());
                return readingType;
            } catch (UnderlyingSQLFailedException e) {
                // maybe some other thread beat us in the race, if not rethrow exception
                return dataModel.mapper(ReadingType.class).getOptional(code).orElseThrow(() -> e);
            }
        }
    }

    private Channel findOrCreateChannel(Reading reading, ChannelsContainer channelsContainer) {
        ReadingType readingType = Objects.requireNonNull(readingTypes.get(reading.getReadingTypeCode()));
        for (Channel each : channelsContainer.getChannels()) {
            if (each.getReadingTypes().contains(readingType)) {
                return each;
            }
        }
        return channelsContainer.createChannel(readingType);
    }

    private Channel findOrCreateChannel(IntervalReading reading, ReadingType readingType) {
        Channel channel = getChannel(reading, readingType);
        if (channel == null) {
            for (ChannelsContainer channelsContainer : meter.getChannelsContainers()) {
                if (channelsContainer.getInterval().toOpenClosedRange().contains(reading.getTimeStamp())) {
                    return channelsContainer.createChannel(readingType);
                }
            }
            PrivateMessageSeeds.NOMETERACTIVATION.log(logger, thesaurus, meter.getName(), reading.getTimeStamp());
            return null;
        } else {
            return channel;
        }
    }

    private Channel getChannel(IntervalReading reading, ReadingType readingType) {
        for (ChannelsContainer channelsContainer : meter.getChannelsContainers()) {
            if (channelsContainer.getInterval().toOpenClosedRange().contains(reading.getTimeStamp())) {
                for (Channel channel : channelsContainer.getChannels()) {
                    if (channel.getReadingTypes().contains(readingType)) {
                        return channel;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private void addedReading(Channel channel, BaseReading reading) {
        channelReadings.computeIfAbsent(channel, key -> new HashMap<>()).put(reading.getTimeStamp(), reading);
    }

    private void storeReadingQualities() {
        dataModel.mapper(ReadingQualityRecord.class).persist(
                channelReadings.entrySet().stream()
                        .flatMap(entry -> buildReadingQualities(entry.getKey(), entry.getValue().values()))
                        .collect(Collectors.toList()));
    }

    private Stream<ReadingQualityRecord> buildReadingQualities(Channel channel, Collection<BaseReading> readings) {
        return readings.stream().flatMap(reading -> buildReadingQualities(channel, reading));
    }

    private Stream<ReadingQualityRecord> buildReadingQualities(Channel channel, BaseReading reading) {
        return reading.getReadingQualities().stream().map(readingQuality -> buildReadingQualityRecord(channel, reading, readingQuality));
    }

    private ReadingQualityRecord buildReadingQualityRecord(Channel channel, BaseReading reading, ReadingQuality readingQuality) {
        CimChannel cimChannel = channel.getCimChannel(channel.getMainReadingType())
                // should never happen normally
                .orElseThrow(() -> new IllegalArgumentException("Channel " + channel.getId() + " has its main reading type but doesn't return a CimChannel for it"));
        if (reading instanceof Reading) {
            Optional<ReadingType> found = meteringService.getReadingType(((Reading) reading).getReadingTypeCode());
            if (found.isPresent()) {
                ReadingType readingType = found.get();
                if (!cimChannel.getReadingType().equals(readingType)) {
                    cimChannel = channel.getCimChannel(readingType).orElse(cimChannel);
                }
            }
        }
        ReadingQualityRecordImpl newReadingQuality = ReadingQualityRecordImpl.from(dataModel, readingQuality.getType(), cimChannel, reading.getTimeStamp());
        newReadingQuality.setComment(readingQuality.getComment());
        return newReadingQuality;
    }
}
