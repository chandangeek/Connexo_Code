package com.elster.jupiter.metering;

import com.elster.jupiter.ids.TimeSeries;

import java.util.Date;
import java.util.List;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
	TimeSeries getTimeSeries();
	List<ReadingType> getReadingTypes();
	List<IntervalReading> getIntervalReadings(Date from , Date to);
	List<Reading> getRegisterReadings(Date from, Date to);
	ReadingType getMainReadingType();
	ReadingType getCumulativeReadingType();

    long getVersion();

    List<IntervalReading> getIntervalReadings(ReadingType readingType, Date from, Date to);
}
