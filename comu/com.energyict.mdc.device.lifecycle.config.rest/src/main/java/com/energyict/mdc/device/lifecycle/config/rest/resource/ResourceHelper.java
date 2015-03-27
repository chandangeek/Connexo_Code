package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;

import javax.inject.Inject;
import java.util.Objects;

public class ResourceHelper {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            ExceptionFactory exceptionFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.exceptionFactory = exceptionFactory;
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

}