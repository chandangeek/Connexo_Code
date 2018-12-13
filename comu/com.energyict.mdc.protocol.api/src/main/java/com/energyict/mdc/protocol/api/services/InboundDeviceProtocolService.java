/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

import java.util.Collection;

public interface InboundDeviceProtocolService {

    /**
     * Create an InboundDeviceProtocol for the given javaClass of the pluggableClass
     *
     * @param pluggableClass the pluggableClass to use as model for the InboundDeviceProtocol
     * @return the created InboundDeviceProtocol
     */
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass);

    /**
     * Create an InboundDeviceProtocol for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the InboundDeviceProtocol
     * @return the created InboundDeviceProtocol
     */
    public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName);

    /**
     * Return a list of all known InboundDeviceProtocol pluggable classes
     * @return list of all known InboundDeviceProtocol pluggable classes
     */
    public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses();

}
