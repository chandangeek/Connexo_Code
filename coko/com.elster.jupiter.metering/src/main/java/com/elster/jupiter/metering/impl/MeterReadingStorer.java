package com.elster.jupiter.metering.impl;

import java.util.List;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

public class MeterReadingStorer {
	private final ReadingStorer readingStorer;
	private final MeterReadingFacade facade;
	private final Meter meter;
	
	MeterReadingStorer(Meter meter , MeterReading meterReading) {
		this.meter= meter;
		this.facade = new MeterReadingFacade(meterReading);
		this.readingStorer = (ReadingStorerImpl) Bus.getMeteringService().createOverrulingStorer();
	}
	
	void store() {
		List<MeterActivation> meterActivations = meter.getMeterActivations();
		if (meterActivations.isEmpty()) {
			createDefaultMeterActivation();
		}
		storeReadings(facade.getMeterReading().getReadings());
		storeIntervalBlocks(facade.getMeterReading().getIntervalBlocks());
	}
	
	void createDefaultMeterActivation() {
		meter.activate(facade.getInterval().getStart());
	}
	
	void storeReadings(List<Reading> readings) {
		for (Reading reading : readings) {
			store(reading);
		}
	}
	
	void store(Reading reading) {
		for (MeterActivation meterActivation : meter.getMeterActivations()) {
			if (meterActivation.getInterval().contains(reading.getTimeStamp(),Interval.EndpointBehavior.CLOSED_CLOSED)) {
					store(reading , meterActivation);
			}
		}
	}
	
	void store(Reading reading, MeterActivation meterActivation) {
		Channel channel = findOrCreateChannel(reading,meterActivation);
		if (channel != null) {
			readingStorer.addReading(channel,reading);
		}					
	}
	
	void storeIntervalBlocks(List<IntervalBlock> blocks) {
		for (IntervalBlock block : blocks) {
			store(block);
		}
	}
	
	void store(IntervalBlock block) {
		String readingTypeCode = block.getReadingTypeCode();
		for (IntervalReading each : block.getIntervals()) {
			store(each,readingTypeCode);
		}
	}
	
	void store(IntervalReading reading , String readingTypeCode) {
		Channel channel = getChannel(reading, readingTypeCode);
		if (channel != null) {
			readingStorer.addIntervalReading(channel, reading.getTimeStamp(), 0, reading.getValue());
		}
	}
	
	Optional<ReadingType> getReadingType(String code) {
		return Bus.getOrmClient().getReadingTypeFactory().get(code);				
	}
	
	Channel findOrCreateChannel(Reading reading , MeterActivation meterActivation) {
		for (Channel each : meterActivation.getChannels()) {
			if (each.getMainReadingType().getMRID().equals(reading.getReadingTypeCode())) {
				return each;
			}
		}
		Optional<ReadingType> readingTypeHolder = getReadingType(reading.getReadingTypeCode());
		if (readingTypeHolder.isPresent()) {
			return meterActivation.createChannel(readingTypeHolder.get());
		} else {
			return null;
		}
	}
	
	Channel getChannel(IntervalReading reading, String readingTypeCode) {
		for (MeterActivation meterActivation : meter.getMeterActivations()) {
			if (meterActivation.getInterval().contains(reading.getTimeStamp(),Interval.EndpointBehavior.OPEN_CLOSED)) {
				for (Channel channel : meterActivation.getChannels()) {
					if (channel.getMainReadingType().getMRID().equals(readingTypeCode)) {
						return channel;
					}
				}
			}
		}
		return null;
	}
	
	
 }
