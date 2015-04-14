package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public Integer statesCount;
    public Integer actionsCount;

    public DeviceLifeCycleInfo() {}

    public DeviceLifeCycleInfo(DeviceLifeCycle lifeCycle) {
        this.id = lifeCycle.getId();
        this.name = lifeCycle.getName();
        this.statesCount = lifeCycle.getFiniteStateMachine().getStates().size();
        this.actionsCount = lifeCycle.getAuthorizedActions().size();
        this.version = lifeCycle.getVersion();
    }
}
