package com.energyict.mdc.device.lifecycle.config.rest.info;


public class DeviceLifeCycleStateSummaryInfo {
    public long deviceLifeCycleId;
    public String name;
    public String deviceState;

    public DeviceLifeCycleStateSummaryInfo(long deviceLifeCycleId, String name, String deviceState){
        this.deviceLifeCycleId = deviceLifeCycleId;
        this.name = name;
        this.deviceState = deviceState;
    }
}
