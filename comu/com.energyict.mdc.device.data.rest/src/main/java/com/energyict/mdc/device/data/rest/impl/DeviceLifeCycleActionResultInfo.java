package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;

import java.util.List;

public class DeviceLifeCycleActionResultInfo {
    public boolean success = true; // default = true
    public String message;
    public List<IdWithNameInfo> microChecks;

    public DeviceLifeCycleActionResultInfo() {}


}
