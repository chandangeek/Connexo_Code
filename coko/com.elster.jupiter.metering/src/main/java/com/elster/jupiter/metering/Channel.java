package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();

    Optional<TemporalAmount> getIntervalLength();

    List<? extends ReadingType> getReadingTypes();
    List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval);

    List<BaseReadingRecord> getReadingsUpdatedSince(ReadingType readingType, Range<Instant> interval, Instant since);

    List<ReadingRecord> getRegisterReadings(Range<Instant> interval);
    List<BaseReadingRecord> getReadings(Range<Instant> interval);
	ReadingType getMainReadingType();
	Optional<? extends ReadingType> getBulkQuantityReadingType();
    long getVersion();
    List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval);
    List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval);
    List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval);
    Optional<BaseReadingRecord> getReading(Instant when);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, BaseReading baseReading);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, Instant timestamp);
    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> interval);
    List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval);
    List<ReadingQualityRecord> findReadingQuality(Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(Range<Instant> interval);
    List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> interval);
    boolean isRegular();
    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);
    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);
    boolean hasMacroPeriod();
    boolean hasData();
	void editReadings(List<? extends BaseReading> readings);
    void confirmReadings(List<? extends BaseReading> readings);
	void removeReadings(List<? extends BaseReadingRecord> readings);
	Instant getFirstDateTime();
	Instant getLastDateTime();
    Instant getNextDateTime(Instant instant);
    Instant getPreviousDateTime(Instant instant);
	List<Instant> toList(Range<Instant> range);
	default boolean hasReadingType(ReadingType readingType) {
		return getReadingTypes().contains(readingType);
	}

    ZoneId getZoneId();

    Optional<CimChannel> getCimChannel(ReadingType readingType);

    interface ReadingsDeletedEvent {
		Channel getChannel();
		Set<Instant> getReadingTimeStamps();
		default Range<Instant> getRange() {
			return Range.encloseAll(getReadingTimeStamps());
		}
	}

	
}
