package com.energyict.mdc.protocol.api;

/**
 * Models a {@link DeviceProtocol} that was registered in the HeadEnd as a {@link PluggableClass}.
 *
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 8:58
 */
public interface DeviceProtocolPluggableClass extends PluggableClass<DeviceProtocol> {

    public DeviceProtocol getDeviceProtocol ();

}