package com.elster.jupiter.bpm.rest;


public class DeviceLifeCycleStateSummaryInfo {
    public long deviceStateId;
    public long deviceLifeCycleId;
    public String name;
    public String deviceState;

    public DeviceLifeCycleStateSummaryInfo(){

    }

    public DeviceLifeCycleStateSummaryInfo(long deviceLifeCycleId, long deviceStateId, String name, String deviceState){
        this.deviceStateId = deviceStateId;
        this.deviceLifeCycleId = deviceLifeCycleId;
        this.name = name;
        this.deviceState = deviceState;
    }
}