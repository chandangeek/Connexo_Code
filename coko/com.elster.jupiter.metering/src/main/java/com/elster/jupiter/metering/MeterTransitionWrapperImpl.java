package com.elster.jupiter.metering;

import java.time.Instant;

public class MeterTransitionWrapperImpl implements MeterTransitionWrapper {

    EndDevice device;
    Instant instant;

    public MeterTransitionWrapperImpl(EndDevice device, Instant instant){
        this.device = device;
        this.instant = instant;
    }
    @Override
    public EndDevice getEndDevice(){
        return device;
    }

    @Override
    public Instant getInstant(){
        return instant;
    }
}
