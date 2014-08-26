package com.elster.jupiter.metering.readings;

import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface BaseReading {
	BigDecimal getSensorAccuracy();
	Date getTimeStamp();
	Date getReportedDateTime();
	BigDecimal getValue();
	String getSource();
	Interval getTimePeriod();
	List<? extends ReadingQuality> getQualities();
}
