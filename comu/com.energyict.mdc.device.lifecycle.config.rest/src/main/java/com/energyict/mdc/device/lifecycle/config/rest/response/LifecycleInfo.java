package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import java.util.Objects;

public class LifecycleInfo {
    public Long id;
    public String name;

    public LifecycleInfo() {}

    public LifecycleInfo(DeviceLifeCycle lifeCycle) {
        Objects.requireNonNull(lifeCycle);
        this.id = lifeCycle.getId();
        this.name = lifeCycle.getName();
    }
}
