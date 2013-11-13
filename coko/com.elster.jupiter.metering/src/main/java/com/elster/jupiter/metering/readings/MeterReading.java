package com.elster.jupiter.metering.readings;

import java.util.List;

public interface MeterReading {
	List<Reading> getReadings();
	List<IntervalBlock> getIntervalBlocks();
	List<EndDeviceEvent> getEvents();
}
