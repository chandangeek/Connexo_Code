package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;

import java.time.Instant;
import java.util.List;

/**
 * Provides an implementation for the {@link ExecutableAction} interface
 * for {@link AuthorizedBusinessProcessAction}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-24 (09:01)
 */
public class ExecutableBusinessProcessActionImpl implements ExecutableAction {
    private final Device device;
    private final AuthorizedBusinessProcessAction action;
    private final DeviceLifeCycleService service;

    public ExecutableBusinessProcessActionImpl(Device device, AuthorizedBusinessProcessAction action, DeviceLifeCycleService service) {
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
    public void execute(Instant effectiveTimestamp, List<ExecutableActionProperty> properties) throws SecurityException, DeviceLifeCycleActionViolationException {
        this.service.execute(this.action, this.device, Instant.now());
    }

}