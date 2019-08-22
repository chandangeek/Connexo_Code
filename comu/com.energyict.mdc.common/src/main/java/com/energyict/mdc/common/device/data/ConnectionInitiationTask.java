/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.energyict.mdc.common.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;

import aQute.bnd.annotation.ConsumerType;

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
@ConsumerType
public interface ConnectionInitiationTask extends OutboundConnectionTask<PartialConnectionInitiationTask> {

    /**
     * Gets the {@link ConnectionTaskProperty} with the specified name
     * or <code>null</code> if no such property exists.
     *
     * @param name The property name
     * @return The ConnectionTaskProperty
     */
    ConnectionTaskProperty getProperty(String name);

}