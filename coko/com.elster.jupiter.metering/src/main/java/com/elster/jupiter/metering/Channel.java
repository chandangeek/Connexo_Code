package com.elster.jupiter.metering;

import com.elster.jupiter.ids.TimeSeries;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
	TimeSeries getTimeSeries();
    List<ReadingType> getReadingTypes();
    List<IntervalReadingRecord> getIntervalReadings(Date from , Date to);
    List<ReadingRecord> getRegisterReadings(Date from, Date to);
    List<BaseReadingRecord> getReadings(Date from, Date to);
	ReadingType getMainReadingType();
	ReadingType getCumulativeReadingType();
    long getVersion();
    List<ReadingRecord> getRegisterReadings(ReadingType readingType, Date from, Date to);
    List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Date from, Date to);
    List<BaseReadingRecord> getReadings(ReadingType readingType, Date from, Date to);
    Optional<BaseReadingRecord> getReading(Date when);
    ReadingQuality createReadingQuality(ReadingQualityType type, BaseReadingRecord baseReadingRecord);
}
