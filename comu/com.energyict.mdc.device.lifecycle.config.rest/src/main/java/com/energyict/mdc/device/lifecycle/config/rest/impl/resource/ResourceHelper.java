/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class ResourceHelper {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final ExceptionFactory exceptionFactory;
    private final EventService eventService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ResourceHelper(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            DeviceConfigurationService deviceConfigurationService,
            FiniteStateMachineService finiteStateMachineService,
            ExceptionFactory exceptionFactory,
            EventService eventService,
            ConcurrentModificationExceptionFactory conflictFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.exceptionFactory = exceptionFactory;
        this.eventService = eventService;
        this.conflictFactory = conflictFactory;
    }

    DeviceLifeCycle findDeviceLifeCycleByIdOrThrowException(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_NOT_FOUND, id));
    }

    private Long getCurrentDeviceLifeCycleVersion(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id)
                .map(DeviceLifeCycle::getVersion).orElse(null);
    }

    private Optional<DeviceLifeCycle> getLockedDeviceLifeCycle(long id, long version) {
        return deviceLifeCycleConfigurationService.findAndLockDeviceLifeCycleByIdAndVersion(id, version);
    }

    DeviceLifeCycle lockDeviceLifeCycleOrThrowException(DeviceLifeCycleInfo info) {
        return getLockedDeviceLifeCycle(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentDeviceLifeCycleVersion(info.id))
                        .supplier());
    }

    State findStateByIdOrThrowException(DeviceLifeCycle deviceLifeCycle, long stateId) {
        Objects.requireNonNull(deviceLifeCycle);
        return deviceLifeCycle.getFiniteStateMachine().getStates()
                .stream()
                .filter(state -> state.getId() == stateId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_NOT_FOUND, stateId));
    }

    private Long getCurrentStateVersion(long id) {
        return finiteStateMachineService.findFiniteStateById(id).map(State::getVersion).orElse(null);
    }

    private Optional<State> getLockedStateOrTrowException(long id, long version) {
        return finiteStateMachineService.findAndLockStateByIdAndVersion(id, version);
    }

    State lockStateOrThrowException(DeviceLifeCycleStateInfo info) {
        Optional<DeviceLifeCycle> deviceLifeCycle = getLockedDeviceLifeCycle(info.parent.id, info.parent.version);
        if (deviceLifeCycle.isPresent()) {
            return getLockedStateOrTrowException(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceLifeCycleVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentStateVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceLifeCycleVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentStateVersion(info.id))
                .build();
    }

    AuthorizedAction findAuthorizedActionByIdOrThrowException(DeviceLifeCycle deviceLifeCycle, long actionId) {
        Objects.requireNonNull(deviceLifeCycle);
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(action -> action.getId() == actionId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND, actionId));
    }

    private Long getCurrentAuthorizedActionVersion(long id) {
        return deviceLifeCycleConfigurationService.findAuthorizedActionById(id).map(AuthorizedAction::getVersion).orElse(null);
    }

    private Optional<AuthorizedAction> getLockedAuthorizedAction(long id, long version) {
        return deviceLifeCycleConfigurationService.findAndLockAuthorizedActionByIdAndVersion(id, version);
    }

    public AuthorizedAction lockAuthorizedActionOrThrowException(AuthorizedActionInfo info) {
        Optional<DeviceLifeCycle> deviceLifeCycle = getLockedDeviceLifeCycle(info.parent.id, info.parent.version);
        if (deviceLifeCycle.isPresent()) {
            return getLockedAuthorizedAction(info.id, info.version)
                    .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                            .withActualParent(() -> getCurrentDeviceLifeCycleVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentAuthorizedActionVersion(info.id))
                            .supplier());
        }
        throw conflictFactory.contextDependentConflictOn(info.name)
                .withActualParent(() -> getCurrentDeviceLifeCycleVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentAuthorizedActionVersion(info.id))
                .build();
    }

    public Optional<StateTransitionEventType> findStateTransitionEventType(String symbol){
        Optional<EventType> eventType = eventService.getEventType(symbol);
        Optional<? extends StateTransitionEventType> stateTransitionEventType;
        if (eventType.isPresent()){
            stateTransitionEventType = finiteStateMachineService.findStandardStateTransitionEventType(eventType.get());
        } else {
            stateTransitionEventType = finiteStateMachineService.findCustomStateTransitionEventType(symbol);
        }
        return Optional.ofNullable(stateTransitionEventType.orElse(null));
    }

    void checkDeviceLifeCycleUsages(DeviceLifeCycle deviceLifeCycle) {
        if (!deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(deviceLifeCycle).isEmpty()){
            throw exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_IS_USED_BY_DEVICE_TYPE);
        }
    }

}