package com.energyict.mdc.device.config.events;

import com.energyict.mdc.device.config.PartialConnectionTask;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

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
    public long getId();

    /**
     * Gets the {@link PartialConnectionTask} that was updated.
     *
     * @return The PartialConnectionTask
     */
    public PartialConnectionTask getPartialConnectionTask();

    /**
     * Gets the list of required properties
     * that were removed during the update session.
     *
     * @return The list of required properties
     */
    public List<String> getRemovedRequiredProperties();

    /**
     * Gets the comma separated list of required properties
     * that were removed during the update session.
     *
     * @return The comma separated list of required properties
     */
    public String getRemovedRequiredPropertiesAsString();

}