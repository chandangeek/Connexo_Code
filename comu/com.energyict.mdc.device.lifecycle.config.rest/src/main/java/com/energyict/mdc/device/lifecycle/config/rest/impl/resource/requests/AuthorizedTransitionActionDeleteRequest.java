/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;

import java.util.Objects;

public class AuthorizedTransitionActionDeleteRequest implements AuthorizedActionChangeRequest {
    private final AuthorizedTransitionAction obsoleteAction;
    private final DeviceLifeCycle deviceLifeCycle;

    public AuthorizedTransitionActionDeleteRequest(DeviceLifeCycle deviceLifeCycle, AuthorizedTransitionAction obsoleteAction) {
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(obsoleteAction);
        this.obsoleteAction = obsoleteAction;
        this.deviceLifeCycle = deviceLifeCycle;
    }

    @Override
    public AuthorizedAction perform() {
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.removeTransitionAction(this.obsoleteAction.getStateTransition());
        deviceLifeCycleUpdater.complete();
        this.deviceLifeCycle.save();

        StateTransitionEventType eventType = this.obsoleteAction.getStateTransition().getEventType();
        FiniteStateMachineUpdater finiteStateMachineUpdater = this.deviceLifeCycle.getFiniteStateMachine().startUpdate();
        finiteStateMachineUpdater
                .state(this.obsoleteAction.getState().getId())
                .prohibit(eventType)
                .complete();
        finiteStateMachineUpdater.complete();

        return this.obsoleteAction;
    }
}
