package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceLifeCycleChangeEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:54)
 */
public class DeviceLifeCycleChangeEventImpl implements DeviceLifeCycleChangeEvent {

    private final DeviceLifeCycleInDeviceType entity;

    public DeviceLifeCycleChangeEventImpl(DeviceLifeCycleInDeviceType entity) {
        super();
        this.entity = entity;
    }

    @Override
    public Instant getTimestamp() {
        return this.entity.getInterval().getStart();
    }

    @Override
    public DeviceLifeCycle getDeviceLifeCycle() {
        return this.entity.getDeviceLifeCycle();
    }

    @Override
    public DeviceType getDeviceType() {
        return this.entity.getDeviceType();
    }

    @Override
    public Optional<User> getUser() {
        return this.entity.getUser();
    }

}