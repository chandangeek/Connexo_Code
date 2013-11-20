package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.IntervalReading;

public interface IntervalReadingRecord extends BaseReadingRecord , IntervalReading {
	long getProfileStatus();
}
