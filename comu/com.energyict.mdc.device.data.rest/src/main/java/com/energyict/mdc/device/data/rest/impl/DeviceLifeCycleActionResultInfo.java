package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;

import java.util.List;

public class DeviceLifeCycleActionResultInfo {
    public boolean result = true; // default = true, don't change to 'success'!
    public String targetState;
    public String message;
    public boolean transitionNow = true;
    public List<IdWithNameInfo> microChecks;

    public DeviceLifeCycleActionResultInfo() {}


}
