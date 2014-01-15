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

    public DeviceProtocol getDeviceProtocol ();

}