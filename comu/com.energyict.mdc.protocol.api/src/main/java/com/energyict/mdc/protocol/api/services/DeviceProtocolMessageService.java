package com.energyict.mdc.protocol.api.services;

/**
 * OSGI Service wrapper to create an instance of a DeviceProtocol Message object.
 *
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:07
 */
public interface DeviceProtocolMessageService {

    /**
     * Create a DeviceProtocol messages related object
     * for the given javaClassName
     *
     * @param javaClassName the javaClassName to use as model for the DeviceProtocol messages related object
     * @return the created DeviceProtocolPluggableClass
     */
    Object createDeviceProtocolMessagesFor(String javaClassName);

}