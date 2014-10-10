package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.MeterReading;
import java.util.Optional;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface Meter extends EndDevice, ReadingContainer {

    String TYPE_IDENTIFIER = "M";

    void store(MeterReading reading);

    List<? extends MeterActivation> getMeterActivations();

    MeterActivation activate(Instant start);

    Optional<? extends MeterActivation> getCurrentMeterActivation();

    Optional<? extends MeterActivation> getMeterActivation(Instant when);

    List<? extends ReadingQualityRecord> getReadingQualities(Range<Instant> range);

}