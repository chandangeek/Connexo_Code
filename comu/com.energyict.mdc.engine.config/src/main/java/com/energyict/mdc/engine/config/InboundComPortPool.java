/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import java.util.List;

/**
 * Models a collection of {@link com.energyict.mdc.engine.config.InboundComPort}s that will
 * all use the same discovery protocol.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (09:17)
 */
public interface InboundComPortPool extends ComPortPool {

    /**
     * Gets the list of {@link InboundComPort} available through this pool.
     *
     * @return The list of InboundComPorts
     */
    public List<InboundComPort> getComPorts();

    /**
     * Gets the {@link InboundDeviceProtocolPluggableClass}.
     *
     * @return The discovery pluggable class
     */
    public InboundDeviceProtocolPluggableClass getDiscoveryProtocolPluggableClass();

    /**
     * Sets the {@link InboundDeviceProtocolPluggableClass}.
     * Note that this is a required attribute of an InboundComPortPool
     * so setting it to <code>null</code> will result in a business exception.
     *
     * @param inboundDeviceProtocolPluggableClass The InboundDeviceProtocolPluggableClass
     */
    public void setDiscoveryProtocolPluggableClass(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass);

}