package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Optional;
import java.util.Date;
import java.util.List;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
    List<? extends ReadingType> getReadingTypes();
    List<IntervalReadingRecord> getIntervalReadings(Interval interval);
    List<ReadingRecord> getRegisterReadings(Interval interval);
    List<BaseReadingRecord> getReadings(Interval interval);
	ReadingType getMainReadingType();
	Optional<? extends ReadingType> getBulkQuantityReadingType();

    long getVersion();

    List<ReadingRecord> getRegisterReadings(ReadingType readingType, Interval interval);
    List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Interval interval);
    List<BaseReadingRecord> getReadings(ReadingType readingType, Interval interval);
    Optional<BaseReadingRecord> getReading(Instant when);
    default Optional<BaseReadingRecord> getReading(Date when) {
    	return getReading(when.toInstant());
    }
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp);

    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Interval interval);

    List<ReadingQualityRecord> findReadingQuality(Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(Interval interval);
    List<ReadingQualityRecord> findActualReadingQuality(Interval interval);

    boolean isRegular();
    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);
    default List<BaseReadingRecord> getReadingsBefore(Date when, int readingCount) {
    	return getReadingsBefore(when.toInstant(), readingCount);
    }
    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);
    default List<BaseReadingRecord> getReadingsOnOrBefore(Date when, int readingCount) {
    	return getReadingsOnOrBefore(when.toInstant(),readingCount);
    }
	boolean hasMacroPeriod();
    boolean hasData();
	void editReadings(List<? extends BaseReading> readings);
	void removeReadings(List<? extends BaseReadingRecord> readings);
	Instant getLastDateTime();
}
