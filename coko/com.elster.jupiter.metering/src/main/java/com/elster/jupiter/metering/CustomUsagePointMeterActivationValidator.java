package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface CustomUsagePointMeterActivationValidator {
    void validateActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws
            CustomUsagePointMeterActivationValidationException;
}
