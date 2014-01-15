package com.elster.jupiter.metering.impl;

import java.util.Date;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.IntervalBuilder;

public class MeterReadingFacade {
	private final MeterReading meterReading;
	private final Interval interval;
	
	MeterReadingFacade(MeterReading meterReading) {
		this.meterReading = meterReading;
		this.interval = createInterval();
	}
	
	private Interval createInterval() {
		IntervalBuilder builder = new IntervalBuilder();
		for (Reading reading : meterReading.getReadings()) {
			builder.add(reading.getTimeStamp());
		}
		for (IntervalBlock block : meterReading.getIntervalBlocks()) {
			TimeAttribute timeAttribute = ReadingTypeImpl.extractTimeAttribute(block.getReadingTypeCode());
			long length = -timeAttribute.getMinutes() * 60000L;
			for (IntervalReading reading : block.getIntervals()) {			
				builder.add(reading.getTimeStamp(),length);
			}
		}
		for (EndDeviceEvent event : meterReading.getEvents()) {
			builder.add(event.getCreatedDateTime());
		}
		return builder.hasInterval() ? builder.getInterval() : null;			
	}
	
	public Interval getInterval() {
		return interval;
	}
	
	public boolean isEmpty() {
		return interval == null;
	}
	
	public MeterReading getMeterReading() {
		return meterReading;
	}
}
