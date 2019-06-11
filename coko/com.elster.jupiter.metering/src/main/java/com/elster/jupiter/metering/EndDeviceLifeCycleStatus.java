/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

/**
 * Models the {@link State} of an {@link EndDevice} over time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-13 (16:51)
 */
@ConsumerType
public interface EndDeviceLifeCycleStatus extends Effectivity {

    /**
     * Gets the {@link EndDevice} for which {@link State}
     * is maintained over time.
     *
     * @return The EndDevice
     */
    public EndDevice getEndDevice();

    /**
     * Gets the {@link State} of the {@link EndDevice}.
     *
     * @return The EndDevice
     */
    public State getState();

    /**
     * Gets the User that triggered the change.
     *
     * @return The user
     */
    public Optional<User> getUser();

    /**
     * Gets the modification time of the change.
     *
     * @return The instant
     */
    Instant getModTime();

    /**
     * Gets the creation time of the object.
     *
     * @return The instant
     */
    Instant getCreateTime();

    /**
     * Gets the User that originate the status.
     *
     * @return The originator
     */
    Optional<User> getOriginator();

    /**
     * Closes this EndDeviceLifeCycleStatus so that it is
     * only effective until the specified closing Date.
     *
     * @param closingDate The closing Date
     */
    public void close(Instant closingDate);

}