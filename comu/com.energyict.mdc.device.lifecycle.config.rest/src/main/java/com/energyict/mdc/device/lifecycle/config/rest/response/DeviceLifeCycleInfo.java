package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleInfo extends IdWithNameInfo {
    public DeviceLifeCycleInfo() {}

    public DeviceLifeCycleInfo(DeviceLifeCycle lifeCycle) {
        super(lifeCycle.getId(), lifeCycle.getName());
    }
}
