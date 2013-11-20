package com.elster.jupiter.metering.impl;

import java.util.List;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;

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
		storeReadings(facade.getMeterReading().getReadings());
		//storeIntervalReadings();
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
		for (Channel channel : meterActivation.getChannels()) {
			for (ReadingType readingType : channel.getReadingTypes()) {
				if (readingType.getMRID().equals(reading.getReadingTypeCode())) {
					readingStorer.addReading(channel,reading);
				}
			}
		}
	}
}
