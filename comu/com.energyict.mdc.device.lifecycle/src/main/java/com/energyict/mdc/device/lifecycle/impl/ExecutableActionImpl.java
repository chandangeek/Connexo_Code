package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

/**
 * Provides an implementation for the {@link ExecutableAction} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (15:49)
 */
public class ExecutableActionImpl implements ExecutableAction {
    private final Device device;
    private final AuthorizedAction action;
    private final DeviceLifeCycleService service;

    public ExecutableActionImpl(Device device, AuthorizedAction action, DeviceLifeCycleService service) {
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
    public void execute() throws SecurityException, DeviceLifeCycleActionViolationException {
        this.service.execute(this.action, this.device);
    }

}