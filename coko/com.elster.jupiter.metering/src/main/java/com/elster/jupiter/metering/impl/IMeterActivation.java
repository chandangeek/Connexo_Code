package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierUsage;

import java.time.Instant;
import java.util.List;

public interface IMeterActivation extends MeterActivation {

    void doEndAt(Instant end);

    List<MultiplierUsage> getMultiplierUsages(Instant instant);
}
