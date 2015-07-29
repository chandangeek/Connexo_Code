package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides a very simple implementation for the {@link DeviceLifeCycleChangeEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-18 (11:16)
 */
public class DeviceLifeCycleChangeEventSimpleImpl implements DeviceLifeCycleChangeEvent {

    private final Instant timeStamp;
    private final DeviceLifeCycle deviceLifeCycle;
    private final DeviceType deviceType;
    private final Optional<User> user;

    public DeviceLifeCycleChangeEventSimpleImpl(Instant timeStamp, DeviceType deviceType, DeviceLifeCycle deviceLifeCycle, Optional<User> user) {
        super();
        this.timeStamp = timeStamp;
        this.deviceType = deviceType;
        this.deviceLifeCycle = deviceLifeCycle;
        this.user = user;
    }

    @Override
    public Instant getTimestamp() {
        return this.timeStamp;
    }

    @Override
    public DeviceLifeCycle getDeviceLifeCycle() {
        return this.deviceLifeCycle;
    }

    @Override
    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    @Override
    public Optional<User> getUser() {
        return this.user;
    }

}