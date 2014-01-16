package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;

public interface IntervalReadingRecord extends BaseReadingRecord , IntervalReading {
	ProfileStatus getProfileStatus();
}
