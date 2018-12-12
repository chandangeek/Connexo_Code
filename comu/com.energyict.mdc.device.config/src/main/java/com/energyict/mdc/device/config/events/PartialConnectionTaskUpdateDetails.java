/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Models the information that is published when
 * a {@link PartialConnectionTask} is updated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-17 (08:57)
 */
@ProviderType
public interface PartialConnectionTaskUpdateDetails {

    /**
     * Gets the number that uniquely identifies the updated {@link PartialConnectionTask}.
     *
     * @return The unique identifier
     */
    long getId();

    /**
     * Gets the {@link PartialConnectionTask} that was updated.
     *
     * @return The PartialConnectionTask
     */
    PartialConnectionTask getPartialConnectionTask();

    /**
     * Gets the list of required properties that were
     * added or removed during the update session.
     *
     * @return The list of required properties
     */
    List<String> getAddedOrRemovedRequiredProperties();

    /**
     * Gets the previous {@link ConnectionFunction} of the {@link PartialConnectionTask}
     *
     * @return the previous ConnectionFunction
     */
    Optional<ConnectionFunction> getPreviousConnectionFunction();

}