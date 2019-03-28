package com.elster.jupiter.metering;

import java.time.Instant;

public interface MeterTransitionWrapper{

    public EndDevice getEndDevice();
    public Instant getInstant();

}

