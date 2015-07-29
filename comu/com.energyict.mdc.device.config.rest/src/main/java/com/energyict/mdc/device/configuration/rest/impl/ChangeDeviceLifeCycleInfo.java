package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;

import java.util.List;

public class ChangeDeviceLifeCycleInfo {
    public boolean success;
    public String message;
    public long version;
    public DeviceLifeCycleInfo currentDeviceLifeCycle;
    public DeviceLifeCycleInfo targetDeviceLifeCycle;
    public List<DeviceLifeCycleStateInfo> notMappableStates;
}
