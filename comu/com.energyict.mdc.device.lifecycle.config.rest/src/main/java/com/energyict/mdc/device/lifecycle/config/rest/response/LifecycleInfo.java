package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

public class LifecycleInfo extends IdWithNameInfo {
    public LifecycleInfo() {}

    public LifecycleInfo(DeviceLifeCycle lifeCycle) {
        super(lifeCycle.getId(), lifeCycle.getName());
    }
}
