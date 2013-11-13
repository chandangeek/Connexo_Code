package com.elster.jupiter.metering.readings;

import java.math.BigDecimal;
import java.util.Date;

import com.elster.jupiter.util.time.Interval;

public interface BaseReading {
	BigDecimal getSensorAccuracy();
	Date getTimeStamp();
	Date getReportedDateTime();
	BigDecimal getValue();
	String getSource();
	Interval getTimePeriod();
}
