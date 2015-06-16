package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransition;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;

import java.util.Collections;
import java.util.Objects;

public class AuthorizedTransitionActionSimpleEditRequest implements AuthorizedActionChangeRequest {
    private final AuthorizedTransitionAction action;
    private final DeviceLifeCycle deviceLifeCycle;
    private final AuthorizedActionInfo infoForEdit;

    public AuthorizedTransitionActionSimpleEditRequest(DeviceLifeCycle deviceLifeCycle, AuthorizedTransitionAction action, AuthorizedActionInfo infoForEdit) {
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(action);
        Objects.requireNonNull(infoForEdit);
        this.action = action;
        this.deviceLifeCycle = deviceLifeCycle;
        this.infoForEdit = infoForEdit;
    }

    @Override
    public AuthorizedAction perform() {
        FiniteStateMachine finiteStateMachine = this.deviceLifeCycle.getFiniteStateMachine();
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = this.deviceLifeCycle.startUpdate();

        StateTransition targetStateTransition = finiteStateMachine.getTransitions().stream()
                        .filter(this.infoForEdit::isLinkedTo).findFirst().get();
        deviceLifeCycleUpdater.transitionAction(targetStateTransition)
                .clearLevels()
                .clearChecks()
                .clearActions()
                .addAllChecks(this.infoForEdit.getMicroChecks())
                .addAllActions(this.infoForEdit.getMicroActions())
                .addAllLevels(this.infoForEdit.getPrivilegeLevels())
                .complete();
        deviceLifeCycleUpdater.complete();
        this.deviceLifeCycle.save();
        return this.action;
    }
}
