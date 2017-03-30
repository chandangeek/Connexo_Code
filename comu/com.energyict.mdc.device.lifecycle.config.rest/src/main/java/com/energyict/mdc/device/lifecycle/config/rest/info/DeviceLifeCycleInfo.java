/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public Integer statesCount;
    public Integer actionsCount;
    public List<IdWithNameInfo> deviceTypes;
    public boolean obsolete;

    public DeviceLifeCycleInfo() {
    }

    public DeviceLifeCycleInfo(DeviceLifeCycle deviceLifeCycle) {
        this.id = deviceLifeCycle.getId();
        this.name = deviceLifeCycle.getName();
        this.statesCount = deviceLifeCycle.getFiniteStateMachine().getStates().size();
        this.actionsCount = deviceLifeCycle.getAuthorizedActions().size();
        this.version = deviceLifeCycle.getVersion();
        this.obsolete = deviceLifeCycle.isObsolete();
    }
}
