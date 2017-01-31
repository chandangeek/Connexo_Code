/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;


public class DeviceLifeCycleStateSummaryInfo {
    public long deviceStateId;
    public long deviceLifeCycleId;
    public String name;
    public String deviceState;

    public DeviceLifeCycleStateSummaryInfo(long deviceLifeCycleId, long deviceStateId, String name, String deviceState){
        this.deviceStateId = deviceStateId;
        this.deviceLifeCycleId = deviceLifeCycleId;
        this.name = name;
        this.deviceState = deviceState;
    }
}
