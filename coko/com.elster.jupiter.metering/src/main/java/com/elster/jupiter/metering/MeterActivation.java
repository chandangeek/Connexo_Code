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
	Channel createChannel(ReadingType main, ReadingType... readingTypes);
    List<Channel> getChannels();
    List<ReadingType> getReadingTypes();
    List<? extends BaseReading> getReadings(Date from , Date to, ReadingType readingType); // TODO signature do we need ? extends BaseReading or can this be IntervalReading?
	boolean isCurrent();

    void endAt(Date end);

    long getVersion();
}
