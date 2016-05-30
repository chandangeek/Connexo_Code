package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointMeterActivator {

    UsagePointMeterActivator activate(Meter meter, MeterRole meterRole);

    UsagePointMeterActivator clear(MeterRole meterRole);

    void complete();
}
