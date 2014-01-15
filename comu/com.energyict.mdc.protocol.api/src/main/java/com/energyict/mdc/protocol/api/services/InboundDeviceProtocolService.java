package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import java.util.Collection;

/**
 * OSGI Service wrapper for {@link InboundDeviceProtocol}s.
 *
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 11:52
 */
public interface InboundDeviceProtocolService {

    /**
     * Create an InboundDeviceProtocol for the given javaClass of the pluggableClass
     *
     * @param pluggableClass the pluggableClass to use as model for the InboundDeviceProtocol
     * @return the created InboundDeviceProtocol
     */
    InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass);

    /**
     * Create an InboundDeviceProtocol for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the InboundDeviceProtocol
     * @return the created InboundDeviceProtocol
     */
    InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName);

    /**
     * Return a list of all known InboundDeviceProtocol pluggable classes
     * @return list of all known InboundDeviceProtocol pluggable classes
     */
    public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses();


}
