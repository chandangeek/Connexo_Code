/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class ActivateDevicesCommand {

    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Clock clock;

    private List<Device> devices;
    private Map<DeviceLifeCycle, AuthorizedTransitionAction> activeStatePerLifeCycle = new HashMap<>();
    private Instant effectiveTimestamp;
    private Predicate<Device> isTransitionAllowed = device -> true;

    @Inject
    public ActivateDevicesCommand(DeviceService deviceService,
                                  DeviceConfigurationService deviceConfigurationService,
                                  DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                  DeviceLifeCycleService deviceLifeCycleService,
                                  Clock clock) {
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.clock = clock;
    }

    public ActivateDevicesCommand setDevices(List<Device> devices) {
        this.devices = Collections.unmodifiableList(devices);
        return this;
    }

    public ActivateDevicesCommand setTransitionDate(Instant effectiveTimestamp) {
        this.effectiveTimestamp = effectiveTimestamp;
        return this;
    }

    public ActivateDevicesCommand setDeviceTransitionFilter(Predicate<Device> filter) {
        this.isTransitionAllowed = filter;
        return this;
    }

    public void run() {
        if (this.effectiveTimestamp == null) {
            this.effectiveTimestamp = this.clock.instant().minusSeconds(60);
        }
        getDeviceList().stream()
                .filter(this.isTransitionAllowed)
                .forEach(this::accept);
    }

    private List<Device> getDeviceList() {
        return this.devices != null ? this.devices : this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
    }

    private void accept(Device device) {
        AuthorizedTransitionAction activateTransition = findActivateTransitionForDevice(device);
        if (device.getState().isInitial()) {
            List<ExecutableActionProperty> properties = DecoratedStream.decorate(activateTransition.getActions().stream())
                    .flatMap(microAction -> this.deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                    .distinct(PropertySpec::getName)
                    .map(ps -> this.toExecutableActionProperty(ps, this.effectiveTimestamp))
                    .collect(Collectors.toList());
            this.deviceLifeCycleService.execute(activateTransition, device, this.effectiveTimestamp, properties);
        }
    }

    private AuthorizedTransitionAction findActivateTransitionForDevice(Device device) {
        DeviceType deviceType = device.getDeviceType();
        DeviceLifeCycle deviceLifeCycle = deviceType.getDeviceLifeCycle();
        if (deviceLifeCycle == null) {
            deviceLifeCycle = this.deviceLifeCycleConfigurationService.findDefaultDeviceLifeCycle()
                    .orElseThrow(() -> new UnableToCreate("Can't find the default lifecycle."));
            this.deviceConfigurationService.changeDeviceLifeCycle(deviceType, deviceLifeCycle);
        }
        AuthorizedTransitionAction activateTransition = this.activeStatePerLifeCycle.get(deviceLifeCycle);
        if (activateTransition == null) {
            activateTransition = findActivateTransitionForDeviceLifeCycle(deviceLifeCycle);
        }
        return activateTransition;
    }

    private AuthorizedTransitionAction findActivateTransitionForDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle) {
        AuthorizedTransitionAction activateTransition;
        activateTransition = deviceLifeCycle.getAuthorizedActions(deviceLifeCycle.getFiniteStateMachine().getInitialState())
                .stream()
                .filter(action -> action instanceof AuthorizedTransitionAction)
                .map(action -> (AuthorizedTransitionAction) action)
                .filter(action -> action.getStateTransition().getTo().getName().equals(DefaultState.ACTIVE.getKey()))
                .findFirst()
                .orElseThrow(() -> new UnableToCreate("Can't find activate transition."));
        this.activeStatePerLifeCycle.put(deviceLifeCycle, activateTransition);
        return activateTransition;
    }

    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            } else {
                throw new IllegalArgumentException("Unknown or unsupported PropertySpec: " + propertySpec.getName() + " that requires value type: " + propertySpec.getValueFactory().getValueType());
            }
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}
