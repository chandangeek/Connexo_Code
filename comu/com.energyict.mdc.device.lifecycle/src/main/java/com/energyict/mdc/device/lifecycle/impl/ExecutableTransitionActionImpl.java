package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import java.util.List;

/**
 * Provides an implementation for the {@link ExecutableAction} interface
 * for {@link AuthorizedTransitionAction}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (15:49)
 */
public class ExecutableTransitionActionImpl implements ExecutableAction {
    private final Device device;
    private final AuthorizedTransitionAction action;
    private final DeviceLifeCycleService service;

    public ExecutableTransitionActionImpl(Device device, AuthorizedTransitionAction action, DeviceLifeCycleService service) {
        super();
        this.device = device;
        this.action = action;
        this.service = service;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public AuthorizedAction getAction() {
        return action;
    }

    @Override
    public void execute(List<ExecutableActionProperty> properties) throws SecurityException, DeviceLifeCycleActionViolationException {
        this.service.execute(this.action, this.device, properties);
    }

}