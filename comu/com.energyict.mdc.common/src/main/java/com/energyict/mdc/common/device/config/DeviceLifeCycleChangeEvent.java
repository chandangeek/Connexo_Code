/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a change to the {@link DeviceLifeCycle} of a {@link DeviceType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (12:47)
 */
@ConsumerType
public interface DeviceLifeCycleChangeEvent {

    /**
     * Gets the timestamp on which the change occurred.
     *
     * @return The timestamp
     */
    public Instant getTimestamp();

    /**
     * Gets the {@link DeviceLifeCycle} that was used as the current
     * DeviceLifeCycle of the {@link DeviceType} as of the timestamp of
     * this DeviceLifeCycleChangeEvent.
     *
     * @return The DeviceLifeCycle
     */
    public DeviceLifeCycle getDeviceLifeCycle();

    /**
     * Gets the {@link DeviceType} on which the DeviceLifeCycle was changed.
     *
     * @return The DeviceType
     */
    public DeviceType getDeviceType();

    /**
     * Gets the User that triggered the change.
     *
     * @return The user
     */
    public Optional<User> getUser();

}