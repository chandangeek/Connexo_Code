package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterTransitionWrapper;

import java.time.Instant;

class MeterTransitionWrapperImpl implements MeterTransitionWrapper {

    private Meter meter;
    private Instant instant;
    private String mRID;
    private long id;
    private long version;



    public MeterTransitionWrapperImpl(Meter meter, Instant instant){
        this.meter = meter;
        this.instant = instant;
        this.mRID = meter.getMRID();
        this.id = meter.getId();
        this.version = meter.getVersion();
    }
    @Override
    public Meter getMeter(){
        return meter;
    }

    @Override
    public Instant getInstant(){
        return instant;
    }

    public String getMRID(){
        return mRID;
    }

    public long getId(){
        return id;
    }

    public long getVersion(){
        return version;
    }

}
