package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.ids.TimeSeries;

public interface Channel {
	long getId();
	MeterActivation getMeterActivation();
	TimeSeries getTimeSeries();
	List<ReadingType> getReadingTypes();
	List<IntervalReading> getIntervalReadings(Date from , Date to);
	List<Reading> getRegisterReadings(Date from, Date to);
}
