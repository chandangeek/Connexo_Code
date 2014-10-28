package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
    List<? extends ReadingType> getReadingTypes();
    List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval);
    List<ReadingRecord> getRegisterReadings(Range<Instant> interval);
    List<BaseReadingRecord> getReadings(Range<Instant> interval);
	ReadingType getMainReadingType();
	Optional<? extends ReadingType> getBulkQuantityReadingType();
    long getVersion();
    List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval);
    List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval);
    List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval);
    Optional<BaseReadingRecord> getReading(Instant when);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp);
    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> interval);
    List<ReadingQualityRecord> findReadingQuality(Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(Range<Instant> interval);
    List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> interval);
    boolean isRegular();
    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);
    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);
    boolean hasMacroPeriod();
    boolean hasData();
	void editReadings(List<? extends BaseReading> readings);
	void removeReadings(List<? extends BaseReadingRecord> readings);
	Instant getLastDateTime();
	
	interface ReadingsDeletedEvent {
		Channel getChannel();
		Set<Instant> getReadingTimeStamps();
	}
}
