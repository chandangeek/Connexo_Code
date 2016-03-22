package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * OSGI Service wrapper for {@link DeviceProtocol}s.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface DeviceProtocolService {

    public static String COMPONENT_NAME = "PR1"; // Stands for Protocol bundle 1 (as more protocol bundles can follow)

    /**
     * Creates an instance of the protocol of the specified className
     * or throws a {@link com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException}
     * when the class is not actually managed by the Device Protocol service.
     *
     * @param className the fully qualified Class name
     * @return the newly created DeviceProtocol
     */
    public Object createProtocol(String className);

}