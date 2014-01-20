package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeterReadingStorer {
	private final ReadingStorer readingStorer;
	private final MeterReadingFacade facade;
	private final Meter meter;

    private static final Logger logger = Logger.getLogger(MeterReadingStorer.class.getName());
    private final MeteringService meteringService;
    private final DataModel dataModel;

    MeterReadingStorer(DataModel dataModel, MeteringService meteringService, Meter meter, MeterReading meterReading) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.meter= meter;
		this.facade = new MeterReadingFacade(meterReading);
		this.readingStorer = this.meteringService.createOverrulingStorer();
	}
	
	void store() {
		List<MeterActivation> meterActivations = meter.getMeterActivations();
		if (meterActivations.isEmpty()) {
			createDefaultMeterActivation();
		}
		storeReadings(facade.getMeterReading().getReadings());
		storeIntervalBlocks(facade.getMeterReading().getIntervalBlocks());
        storeEvents(facade.getMeterReading().getEvents());

        readingStorer.execute();
	}

    private void storeEvents(List<EndDeviceEvent> events) {
        List<EndDeviceEventRecord> records = new ArrayList<>(events.size());
        for (EndDeviceEvent sourceEvent : events) {
            Optional<EndDeviceEventType> found = dataModel.mapper(EndDeviceEventType.class).getOptional(sourceEvent.getEventTypeCode());
            if (found.isPresent()) {
                EndDeviceEventRecordImpl eventRecord = EndDeviceEventRecordImpl.from(dataModel, meter, found.get(), sourceEvent.getCreatedDateTime());
                for (Map.Entry<String, String> entry : sourceEvent.getEventData().entrySet()) {
                    eventRecord.addProperty(entry.getKey(), entry.getValue());
                }
                records.add(eventRecord);
            } else {
                logger.log(Level.INFO, MessageFormat.format("Ignored event {0} on meter {1}, since it is not defined in the system", sourceEvent.getEventTypeCode(), meter.getMRID()));
            }
        }
        dataModel.mapper(EndDeviceEventRecord.class).persist(records);
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
			if (meterActivation.getInterval().contains(reading.getTimeStamp(),Interval.EndpointBehavior.CLOSED_CLOSED)) {
					store(reading , meterActivation);
			}
		}
	}
	
	private void store(Reading reading, MeterActivation meterActivation) {
		Channel channel = findOrCreateChannel(reading,meterActivation);
		if (channel != null) {
			readingStorer.addReading(channel,reading);
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
			store(each,readingTypeCode);
		}
	}
	
	private void store(IntervalReading reading , String readingTypeCode) {
		Channel channel = findOrCreateChannel(reading, readingTypeCode);
		if (channel != null) {
			readingStorer.addIntervalReading(channel, reading.getTimeStamp(), reading.getProfileStatus(), reading.getValue());
		}
	}
	
	private Optional<ReadingType> getReadingType(String code) {
		return dataModel.mapper(ReadingType.class).getOptional(code);
	}
	
	private Channel findOrCreateChannel(Reading reading , MeterActivation meterActivation) {
		Optional<ReadingType> readingTypeHolder = getReadingType(reading.getReadingTypeCode());
		if (readingTypeHolder.isPresent()) {
			for (Channel each : meterActivation.getChannels()) {
				if (each.getReadingTypes().contains(readingTypeHolder.get())) {
					return each;
				}
			}
			return meterActivation.createChannel(readingTypeHolder.get());
		} else {
			return null;
		}
	}
	
	private Channel findOrCreateChannel(IntervalReading reading , String readingTypeCode) {
		Optional<ReadingType> readingTypeHolder = getReadingType(readingTypeCode);
		if (!readingTypeHolder.isPresent()) {
			return null;
		}
		Channel channel = getChannel(reading,readingTypeHolder.get());
		if (channel == null) {
			for (MeterActivation meterActivation : meter.getMeterActivations()) {
				if (meterActivation.getInterval().contains(reading.getTimeStamp(),Interval.EndpointBehavior.OPEN_CLOSED)) {
					return meterActivation.createChannel(readingTypeHolder.get());
				}
			}
			return null;
		} else {
			return channel;
		}
	}
	
	private Channel getChannel(IntervalReading reading, ReadingType readingType) {
		for (MeterActivation meterActivation : meter.getMeterActivations()) {
			if (meterActivation.getInterval().contains(reading.getTimeStamp(),Interval.EndpointBehavior.OPEN_CLOSED)) {
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
	
	
 }
