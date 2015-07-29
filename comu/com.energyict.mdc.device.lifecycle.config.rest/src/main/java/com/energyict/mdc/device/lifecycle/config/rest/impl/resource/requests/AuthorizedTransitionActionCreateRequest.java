package com.energyict.mdc.device.lifecycle.config.rest.impl.resource.requests;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;

import java.util.Collections;
import java.util.Objects;

public class AuthorizedTransitionActionCreateRequest implements AuthorizedActionChangeRequest {
    private final DeviceLifeCycle deviceLifeCycle;
    private final StateTransitionEventType eventType;
    private final AuthorizedActionInfo infoForCreation;

    public AuthorizedTransitionActionCreateRequest(DeviceLifeCycle deviceLifeCycle, StateTransitionEventType eventType, AuthorizedActionInfo infoForCreation) {
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(infoForCreation);
        this.deviceLifeCycle = deviceLifeCycle;
        this.eventType = eventType;
        this.infoForCreation = infoForCreation;
    }

    @Override
    public AuthorizedAction perform() {
        // Create a new transition for finite state machine
        boolean firstState = deviceLifeCycle.getFiniteStateMachine().getStates().isEmpty();
        FiniteStateMachineUpdater finiteStateMachineUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        State newState = finiteStateMachineUpdater
                .state(this.infoForCreation.fromState.id)
                .on(this.eventType)
                .setName(this.infoForCreation.name)
                .transitionTo(this.infoForCreation.toState.id)
                .complete();
        FiniteStateMachine finiteStateMachine = firstState ? finiteStateMachineUpdater.complete(newState) : finiteStateMachineUpdater.complete();
        finiteStateMachine.save();

        // Create a new authorized action
        StateTransition newStateTransition = finiteStateMachine.getTransitions().stream()
                .filter(this.infoForCreation::isLinkedTo)
                .findFirst()
                .get();
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = this.deviceLifeCycle.startUpdate();
        AuthorizedTransitionAction authorizedAction = deviceLifeCycleUpdater
                .newTransitionAction(newStateTransition)
                .addAllChecks(this.infoForCreation.getMicroChecks())
                .addAllActions(this.infoForCreation.getMicroActions())
                .addAllLevels(this.infoForCreation.getPrivilegeLevels())
                .complete();
        deviceLifeCycleUpdater.complete();
        this.deviceLifeCycle.save();
        return authorizedAction;
    }
}
