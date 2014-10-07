package com.elster.jupiter.metering.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Provider;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
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
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

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
    private final Map<Channel, Map<Date, BaseReading>> channelReadings = new HashMap<>();

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

    void store() {
        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        if (meterActivations.isEmpty()) {
            createDefaultMeterActivation();
        }
        removeOldReadingQualities();
        storeReadings(facade.getMeterReading().getReadings());
        storeIntervalBlocks(facade.getMeterReading().getIntervalBlocks());
        removeOldReadingQualities();
        storeReadingQualities();
        storeEvents(facade.getMeterReading().getEvents());
        readingStorer.execute();
        eventService.postEvent(EventType.METERREADING_CREATED.topic(), new EventSource(meter.getId(), facade.getInterval().getStart().getTime(), facade.getInterval().getEnd().getTime()));
    }

    private void removeOldReadingQualities() {
    	  Interval interval = facade.getInterval();
    	  if (interval != null) {
        	DataMapper<ReadingQualityRecord> mapper = dataModel.mapper(ReadingQualityRecord.class);
        	mapper.remove(
        			meter.getReadingQualities(interval.toClosedRange())
        				.stream()
        				.filter(this::isRelevant)
        				.collect(Collectors.<ReadingQualityRecord>toList()));
        }
    }
    
    
    private boolean isRelevant(ReadingQualityRecord readingQuality) {
    	Map<Date, BaseReading> readingMap = channelReadings.get(readingQuality.getChannel());
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

    private void storeEvents(List<EndDeviceEvent> events) {
        List<EndDeviceEventRecord> toCreate = new ArrayList<>(events.size());
        List<EndDeviceEventRecord> toUpdate = new ArrayList<>();
        for (EndDeviceEvent sourceEvent : events) {
            Optional<EndDeviceEventType> found = dataModel.mapper(EndDeviceEventType.class).getOptional(sourceEvent.getEventTypeCode());
            if (found.isPresent()) {
            	Optional<EndDeviceEventRecord> existing = getEventMapper().getOptional(meter.getId(),sourceEvent.getEventTypeCode(), sourceEvent.getCreatedDateTime());
            	if (existing.isPresent()) {
            		if (existing.get().updateProperties(sourceEvent.getEventData())) {
            			toUpdate.add(existing.get());
            		}
            	} else {
            		EndDeviceEventRecordImpl eventRecord = deviceEventFactory.get().init(meter, found.get(), sourceEvent.getCreatedDateTime());
            		eventRecord.updateProperties(sourceEvent.getEventData());
                    eventRecord.setLogBookId(sourceEvent.getLogBookId());
            		toCreate.add(eventRecord);
                }
            } else {
                MessageSeeds.METER_EVENT_IGNORED.log(logger, thesaurus, sourceEvent.getEventTypeCode(), meter.getMRID());
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

    private DataMapper<EndDeviceEventRecord> getEventMapper() {
        return dataModel.mapper(EndDeviceEventRecord.class);
    }

    private void createDefaultMeterActivation() {
        meter.activate(facade.getInterval().getStart());
    }

    private void storeReadings(List<Reading> readings) {
        for (Reading reading : readings) {
            store(reading);
        }
    }

    private void store(Reading reading) {
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            if (meterActivation.getInterval().contains(reading.getTimeStamp(), Interval.EndpointBehavior.CLOSED_CLOSED)) {
                store(reading, meterActivation);
            }
        }
    }

    private void store(Reading reading, MeterActivation meterActivation) {
        Channel channel = findOrCreateChannel(reading, meterActivation);
        if (channel != null) {
            readingStorer.addReading(channel, reading);
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
        Channel channel = findOrCreateChannel(reading, readingTypeCode);
        if (channel != null) {
            readingStorer.addReading(channel, reading);
            addedReading(channel, reading);
        }
    }

    private ReadingType findOrCreateReadingType(String code) {
    	Optional<ReadingType> readingTypeHolder = dataModel.mapper(ReadingType.class).getOptional(code);
        if (readingTypeHolder.isPresent()) {
        	return readingTypeHolder.get();
        } else {
        	ReadingType readingType = ((MeteringServiceImpl) meteringService).createReadingType(code, "");
        	MessageSeeds.READINGTYPE_ADDED.log(logger, thesaurus, code, meter.getMRID());
        	return readingType;
        }
    }

    private Channel findOrCreateChannel(Reading reading, MeterActivation meterActivation) {
        ReadingType readingType = findOrCreateReadingType(reading.getReadingTypeCode());
        for (Channel each : meterActivation.getChannels()) {
            if (each.getReadingTypes().contains(readingType)) {
                return each;
            }
        }
        return meterActivation.createChannel(readingType);
    }

    private Channel findOrCreateChannel(IntervalReading reading, String readingTypeCode) {
        ReadingType readingType = findOrCreateReadingType(readingTypeCode);
        Channel channel = getChannel(reading, readingType);
        if (channel == null) {
            for (MeterActivation meterActivation : meter.getMeterActivations()) {
                if (meterActivation.getInterval().contains(reading.getTimeStamp(), Interval.EndpointBehavior.OPEN_CLOSED)) {
                    return meterActivation.createChannel(readingType);
                }
            }
            MessageSeeds.NOMETERACTIVATION.log(logger, thesaurus, meter.getMRID(), reading.getTimeStamp());
            return null;
        } else {
            return channel;
        }
    }

    private Channel getChannel(IntervalReading reading, ReadingType readingType) {
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            if (meterActivation.getInterval().contains(reading.getTimeStamp(), Interval.EndpointBehavior.OPEN_CLOSED)) {
                for (Channel channel : meterActivation.getChannels()) {
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
    	channelReadings.computeIfAbsent(channel , key -> new HashMap<>()).put(reading.getTimeStamp(), reading);
    }

    private void storeReadingQualities() {
    	dataModel.mapper(ReadingQualityRecord.class).persist(
    		channelReadings.entrySet().stream()
    			.flatMap(entry -> buildReadingQualities(entry.getKey() , entry.getValue().values()))
    			.collect(Collectors.toList()));
    }

    private Stream<ReadingQualityRecord> buildReadingQualities(Channel channel , Collection<BaseReading> readings) {
    	return readings.stream().flatMap(reading -> buildReadingQualities(channel, reading));
    }

    private Stream<ReadingQualityRecord> buildReadingQualities(Channel channel, BaseReading reading) {
    	return reading.getReadingQualities().stream().map(readingQuality -> buildReadingQualityRecord(channel, reading, readingQuality));
    }

    private ReadingQualityRecord buildReadingQualityRecord(Channel channel, BaseReading reading, ReadingQuality readingQuality) {
    	ReadingQualityRecordImpl newReadingQuality = ReadingQualityRecordImpl.from(dataModel, new ReadingQualityType(readingQuality.getTypeCode()),channel,reading.getTimeStamp());
		newReadingQuality.setComment(readingQuality.getComment());
		return newReadingQuality;
    }


    private Optional<ReadingQualityRecordImpl> find(Date timeStamp, String typeCode, List<? extends ReadingQuality> candidates) {
    	for (ReadingQuality each : candidates) {
    		ReadingQualityRecordImpl readingQuality = (ReadingQualityRecordImpl)  each;
    		if (readingQuality.getReadingTimestamp().equals(timeStamp) && readingQuality.getTypeCode().equals(typeCode)) {
    			return Optional.of(readingQuality);
    		}
    	}
    	return Optional.absent();
    }
}
