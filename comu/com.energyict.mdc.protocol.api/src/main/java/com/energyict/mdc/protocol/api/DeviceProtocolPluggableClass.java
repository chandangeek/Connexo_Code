package com.energyict.mdc.protocol.api;

import com.energyict.mdc.pluggable.PluggableClass;

/**
 * Models a {@link DeviceProtocol} that was registered in the HeadEnd as a {@link PluggableClass}.
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 8:58
 */
public interface DeviceProtocolPluggableClass extends PluggableClass {

    /**
     * Returns the version of the {@link DeviceProtocol} and removes
     * any technical details that relate to development tools.
     *
     * @return The DeviceProtocol version
     */
    public String getVersion ();

    public DeviceProtocol getDeviceProtocol ();

}