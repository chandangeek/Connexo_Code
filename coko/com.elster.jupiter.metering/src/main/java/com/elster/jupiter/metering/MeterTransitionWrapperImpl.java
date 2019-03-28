package com.elster.jupiter.metering;

import java.time.Instant;

public class MeterTransitionWrapperImpl implements MeterTransitionWrapper {

    EndDevice device;
    Instant instant;
    private String mRID;
    private long id;
    private long version;



    public MeterTransitionWrapperImpl(EndDevice device, Instant instant){
        this.device = device;
        this.instant = instant;
        this.mRID = device.getMRID();
        this.id = device.getId();
        this.version = device.getVersion();
    }
    @Override
    public EndDevice getEndDevice(){
        return device;
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
