package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface AggregatedChannel extends Channel {

    List<IntervalReadingRecord> getPersistedIntervalReadings(Range<Instant> interval);

    List<IntervalReadingRecord> getCalculatedIntervalReadings(Range<Instant> interval);

    List<ReadingRecord> getPersistedRegisterReadings(Range<Instant> interval);

    List<ReadingRecord> getCalculatedRegisterReadings(Range<Instant> interval);
}
