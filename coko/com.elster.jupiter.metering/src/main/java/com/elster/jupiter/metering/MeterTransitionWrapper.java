package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface MeterTransitionWrapper{
    Meter getMeter();
    Instant getInstant();
}

