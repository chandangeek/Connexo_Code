package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.readings.MeterReading;
import com.google.common.base.Optional;
import com.google.common.collect.Range;

public interface Meter extends EndDevice , ReadingContainer {

    String TYPE_IDENTIFIER = "M";

    void store(MeterReading reading);

    List<? extends MeterActivation> getMeterActivations();

    MeterActivation activate(Date date);

    Optional<MeterActivation> getCurrentMeterActivation();
    Optional<MeterActivation> getMeterActivation(Date when);
    
    List<? extends ReadingQualityRecord> getReadingQualities(Range<Instant> range);
    
}