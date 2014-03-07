package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface Meter extends EndDevice {

    String TYPE_IDENTIFIER = "M";

    void store(MeterReading reading);

    List<MeterActivation> getMeterActivations();

    MeterActivation activate(Date date);

    Optional<MeterActivation> getCurrentMeterActivation();
    
    List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType);
}
