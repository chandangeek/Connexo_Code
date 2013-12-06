package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.Interval;
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
    List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType); // TODO signature do we need ? extends BaseReading or can this be IntervalReading?
	boolean isCurrent();
    void endAt(Date end);
    long getVersion();
    Interval getInterval();
}
