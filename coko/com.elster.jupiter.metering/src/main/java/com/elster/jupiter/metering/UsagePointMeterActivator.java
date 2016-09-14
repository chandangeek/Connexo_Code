package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface UsagePointMeterActivator {

    UsagePointMeterActivator activate(Meter meter, MeterRole meterRole);

    UsagePointMeterActivator activate(Instant start, Meter meter, MeterRole meterRole);

    UsagePointMeterActivator clear(MeterRole meterRole);

    UsagePointMeterActivator clear(Instant from, MeterRole meterRole);

    /**
     * Indicates that activator should throw successor of {@link UsagePointMeterActivationException} in case of incorrect activation as soon as any violation occurs.
     * (By default it produces javax constraint violations).
     * @return the activator
     */
    UsagePointMeterActivator throwingValidation();

    /**
     * Apply changes. Note that after this operation some meters may have obsolete info regarding meter activations.
     */
    void complete();
}
