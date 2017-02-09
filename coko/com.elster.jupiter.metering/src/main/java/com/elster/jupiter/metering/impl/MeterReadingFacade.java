/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.RangeBuilder;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;


public class MeterReadingFacade {
	private static final long MILLIS_PER_MINUTE = 60000L;
	private final MeterReading meterReading;
	private final Range<Instant> range;
	
	MeterReadingFacade(MeterReading meterReading) {
		this.meterReading = meterReading;
		this.range = createRange();
	}
	
	private long millisToSubtract(String readingTypeCode) {
		return max(ReadingTypeImpl.extractTimeAttribute(readingTypeCode).getMinutes() * MILLIS_PER_MINUTE, MILLIS_PER_MINUTE);
	}
	
	private Range<Instant> createRange() {
		RangeBuilder builder = new RangeBuilder();
		for (Reading reading : meterReading.getReadings()) {
			builder.add(reading.getTimeStamp(), -millisToSubtract(reading.getReadingTypeCode()));
		}
		for (IntervalBlock block : meterReading.getIntervalBlocks()) {
			long length = -millisToSubtract(block.getReadingTypeCode());
			for (IntervalReading reading : block.getIntervals()) {			
				builder.add(reading.getTimeStamp(), length);
			}
		}
		for (EndDeviceEvent event : meterReading.getEvents()) {
			builder.add(event.getCreatedDateTime());
		}
		return builder.hasRange() ? builder.getRange() : null;			
	}

	public Optional<Range<Instant>> getRange() {
		return Optional.ofNullable(range);
	}
	
	public boolean isEmpty() {
		return range == null;
	}
	
	public MeterReading getMeterReading() {
		return meterReading;
	}
	
	List<String> readingTypeCodes() {
		Stream<String> first = meterReading.getReadings().stream().map(Reading::getReadingTypeCode);
		Stream<String> second = meterReading.getIntervalBlocks().stream().map(IntervalBlock::getReadingTypeCode);
		return Stream.concat(first,second).distinct().sorted().collect(Collectors.toList());
	}
}
