package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleInfo {
    public Long id;
    public String name;
    public long version;

    public DeviceLifeCycleInfo() {}

    public DeviceLifeCycleInfo(DeviceLifeCycle lifeCycle) {
        this.id = lifeCycle.getId();
        this.name = lifeCycle.getName();
        this.version = lifeCycle.getVersion();
    }
}
