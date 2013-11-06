package com.energyict.mdc.rest;

import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdw.core.PluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface DeviceProtocolFactoryService {

    /**
     * Create a DeviceProtocolPluggableClass wich contains a DeviceProtocol implementation
     * for the given javaClass of the pluggableClass
     *
     * @param pluggableClass the pluggableClass to use as model for the DeviceProtocolPluggableClass
     * @return the created DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass createDeviceProtocolPluggableClassFor(PluggableClass pluggableClass);
}
