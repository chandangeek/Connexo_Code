package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.IdWithNameInfo;

public class LifeCycleStateInfo extends IdWithNameInfo {
    public LifeCycleStateInfo() {}

    public LifeCycleStateInfo(State lifeCycleState) {
        super(lifeCycleState.getId(), lifeCycleState.getName());
    }
}
