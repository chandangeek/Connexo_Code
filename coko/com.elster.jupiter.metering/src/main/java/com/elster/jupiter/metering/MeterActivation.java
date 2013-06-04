package com.elster.jupiter.metering;

import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface MeterActivation {
	long getId();
	Optional<UsagePoint> getUsagePoint();
	Optional<Meter> getMeter();
	Date getStart();
	Date getEnd();
	Channel createChannel(ReadingType... readingTypes);
	List<Channel> getChannels();
	List<ReadingType> getReadingTypes();
	List<BaseReading> getReadings(Date from , Date to, ReadingType readingType);
	boolean isCurrent();
}
