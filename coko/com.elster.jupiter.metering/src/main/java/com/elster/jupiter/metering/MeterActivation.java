package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;
import java.time.ZoneId;
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
    Instant getStart();
    Instant getEnd();
    ZoneId getZoneId();
    void setUsagePoint(UsagePoint usagePoint);
    void setMeter(Meter meter);

    void advanceStartDate(Instant startDate);
}
