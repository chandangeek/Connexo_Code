package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.readings.MeterReading;
import com.google.common.base.Optional;

public interface Meter extends EndDevice , ReadingContainer {

    String TYPE_IDENTIFIER = "M";

    void store(MeterReading reading);

    List<? extends MeterActivation> getMeterActivations();

    MeterActivation activate(Date date);

    Optional<MeterActivation> getCurrentMeterActivation();
    Optional<MeterActivation> getMeterActivation(Date when);
    
}