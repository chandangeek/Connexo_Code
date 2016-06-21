package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MultiplierUsage;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface IMeterActivation extends MeterActivation {

    void doEndAt(Instant end);

    List<MultiplierUsage> getMultiplierUsages(Instant instant);

    MeterActivationImpl init(Meter meter, MeterRole role, UsagePoint usagePoint, Instant from);

    IMeterActivation init(Meter meter, MeterRole meterRole, UsagePoint usagePoint, Range<Instant> range);
}
