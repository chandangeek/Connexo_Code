package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.List;

public interface MeterActivationBuilder {

    MeterActivationBuilder onUsagePoint(UsagePoint usagePoint);

    MeterActivationBuilder startingAt(Instant start);

    List<MeterActivation> build();
}
