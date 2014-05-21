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

    public static String COMPONENT_NAME = "PR1"; // Stands for PRotocol bundle 1 (as more protocol bundles can follow)

    /**
     * Loads the class with the specified javaClassName.
     *
     * @param javaClassName the javaClassName to use to model the new class
     * @return the newly created DeviceProtocol
     */
    public Class loadProtocolClass(String javaClassName);

}