package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

/**
 * OSGI Service wrapper for {@link DeviceProtocol}s.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface DeviceProtocolService {

    /**
     * Create a {@link DeviceProtocol} for the given javaClass of the {@link DeviceProtocolPluggableClass}.
     *
     * @param pluggableClass the pluggableClass to use as model for the DeviceProtocol
     * @return the created DeviceProtocol
     */
    public DeviceProtocol createDeviceProtocolFor(DeviceProtocolPluggableClass pluggableClass);

    /**
     * Creates a {@link DeviceProtocol} (DeviceProtocolPluggableClass) based on the given javaClassName.
     *
     * @param javaClassName the javaClassName to use to model the new class
     * @return the newly created DeviceProtocol
     */
    public DeviceProtocol createDeviceProtocolFor(String javaClassName);

}