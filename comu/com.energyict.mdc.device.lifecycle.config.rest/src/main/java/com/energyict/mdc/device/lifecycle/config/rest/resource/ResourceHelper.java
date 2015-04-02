package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class ResourceHelper {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final ExceptionFactory exceptionFactory;
    private final EventService eventService;

    @Inject
    public ResourceHelper(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            FiniteStateMachineService finiteStateMachineService,
            ExceptionFactory exceptionFactory,
            EventService eventService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.exceptionFactory = exceptionFactory;
        this.eventService = eventService;
    }

    private void checkKey(Object key, MessageSeeds errText){
        if (key == null){
            throw exceptionFactory.newException(errText, "empty");
        }
    }

    public DeviceLifeCycle findDeviceLifeCycleByIdOrThrowException(long id) {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_NOT_FOUND, id));
    }

    public State findStateByIdOrThrowException(DeviceLifeCycle deviceLifeCycle, long stateId) {
        Objects.requireNonNull(deviceLifeCycle);
        return deviceLifeCycle.getFiniteStateMachine().getStates()
                .stream()
                .filter(state -> state.getId() == stateId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_STATE_NOT_FOUND, stateId));
    }

    public AuthorizedAction findAuthorizedActionByIdOrThrowException(DeviceLifeCycle deviceLifeCycle, long actionId) {
        Objects.requireNonNull(deviceLifeCycle);
        return deviceLifeCycle.getAuthorizedActions()
                .stream()
                .filter(action -> action.getId() == actionId)
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND, actionId));
    }

    public StateTransitionEventType findStateTransitionEventTypeOrThrowException(String symbol){
        checkKey(symbol, MessageSeeds.DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND);
        Optional<EventType> eventType = eventService.getEventType(symbol);
        Optional<? extends StateTransitionEventType> stateTransitionEventType = Optional.empty();
        if (eventType.isPresent()){
            stateTransitionEventType = finiteStateMachineService.findStandardStateTransitionEventType(eventType.get());
        } else {
            stateTransitionEventType = finiteStateMachineService.findCustomStateTransitionEventType(symbol);
        }
        return stateTransitionEventType.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND, symbol));
    }
}