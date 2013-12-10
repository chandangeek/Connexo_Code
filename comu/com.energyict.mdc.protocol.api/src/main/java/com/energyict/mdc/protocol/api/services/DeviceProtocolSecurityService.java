package com.energyict.mdc.protocol.api.services;

/**
 * OSGI Service wrapper to create an instance of a DeviceProtocol SecurityRelated object
 *
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:03
 */
public interface DeviceProtocolSecurityService {

    /**
     * Create a DeviceProtocol security related object
     * for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol security related object
     * @return the created DeviceProtocolPluggableClass
     */
    Object createDeviceProtocolSecurityFor(String javaClassName);

}
