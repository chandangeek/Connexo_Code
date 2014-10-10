package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface MeterActivation extends Effectivity , ReadingContainer {
	long getId();
	Optional<UsagePoint> getUsagePoint();
	Optional<Meter> getMeter();
	Channel createChannel(ReadingType main, ReadingType... readingTypes);
    List<Channel> getChannels();
    List<ReadingType> getReadingTypes();
	boolean isCurrent();
    void endAt(Instant end);
    long getVersion();
    boolean overlaps(Interval interval);
}
