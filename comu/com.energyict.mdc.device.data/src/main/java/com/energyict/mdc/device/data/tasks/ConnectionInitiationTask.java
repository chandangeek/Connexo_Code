package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.PartialConnectionInitiationTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link ConnectionTask} that is designed to initiate
 * the setup of a connection with a device.
 * A practical example is device wake up:
 * very often, the communication layer of devices goes into
 * some kind of sleep mode to save energy and extend the battery life.
 * A special signal is sent to them that reactivates the communication layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-11 (11:36)
 */
@ProviderType
public interface ConnectionInitiationTask extends OutboundConnectionTask<PartialConnectionInitiationTask> {

    /**
     * Gets the {@link ConnectionTaskProperty} with the specified name
     * or <code>null</code> if no such property exists.
     *
     * @param name The property name
     * @return The ConnectionTaskProperty
     */
    public ConnectionTaskProperty getProperty (String name);

}