package com.elster.jupiter.metering;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
	TimeSeries getTimeSeries();
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
    Optional<BaseReadingRecord> getReading(Date when);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReadingRecord baseReadingRecord);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, Date timestamp);

    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Date timestamp);
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Interval interval);

    List<ReadingQualityRecord> findReadingQuality(Date timestamp);
    List<ReadingQualityRecord> findReadingQuality(Interval interval);

    boolean isRegular();
    List<BaseReadingRecord> getReadingsBefore(Date when, int readingCount);
    List<BaseReadingRecord> getReadingsOnOrBefore(Date when, int readingCount);
	boolean hasMacroPeriod();
}
