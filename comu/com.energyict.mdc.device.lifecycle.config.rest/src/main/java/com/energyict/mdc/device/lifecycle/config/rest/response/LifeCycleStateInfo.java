package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

public class LifeCycleStateInfo extends IdWithNameInfo {
    public LifeCycleStateInfo() {}

    public LifeCycleStateInfo(State lifeCycle) {
        super(lifeCycle.getId(), lifeCycle.getName());
    }
}
