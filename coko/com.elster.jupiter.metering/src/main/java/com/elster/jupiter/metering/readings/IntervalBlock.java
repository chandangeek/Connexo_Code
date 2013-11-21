package com.elster.jupiter.metering.readings;

import java.util.List;

public interface IntervalBlock {
	List<IntervalReading> getIntervals();
	String getReadingTypeCode();
}
