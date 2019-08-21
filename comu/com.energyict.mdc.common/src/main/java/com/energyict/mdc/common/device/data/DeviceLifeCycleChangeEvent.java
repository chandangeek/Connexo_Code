/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a change to the {@link DeviceLifeCycle} of a {@link Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (13:02)
 */
@ConsumerType
public interface DeviceLifeCycleChangeEvent {

    public enum Type {
        /**
         * Indicates that the life cycle has changed.
         * Technically, this should imply a change of State
         * but because a change of life cycle is only
         * allowed when all States can be mapped
         * between old and new life cycle
         * the change is not apparent to the user
         * and is therefore not reported as such.
         */
        LIFE_CYCLE,

        /**
         * Indicates that the state of the device changed.
         */
        STATE;
    }

    /**
     * Gets the timestamp on which the change occurred.
     *
     * @return The timestamp
     */
    public Instant getTimestamp();

    /**
     * Gets the type of this change.
     *
     * @return The Type
     */
    public Type getType();

    /**
     * Gets the new {@link DeviceLifeCycle}
     * that was set on the {@link DeviceType}.
     * Note that this will throw a NoSuchElementException when
     * the type of this change is not {@link Type#LIFE_CYCLE}.
     *
     * @return The new DeviceLifeCycle
     */
    public DeviceLifeCycle getDeviceLifeCycle();

    /**
     * Gets the new {@link State} that was in effect on the Device.
     * Note that this will throw a NoSuchElementException when
     * the type of this change is not {@link Type#STATE}.
     *
     * @return The new State
     */
    public State getState();

    /**
     * Gets the User that triggered the change.
     *
     * @return The user
     */
    public Optional<User> getUser();

}