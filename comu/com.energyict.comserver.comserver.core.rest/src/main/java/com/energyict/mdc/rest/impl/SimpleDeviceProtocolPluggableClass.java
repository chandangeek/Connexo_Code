package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdw.core.PluggableClass;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 16:23
 */
public class SimpleDeviceProtocolPluggableClass {

    private final PluggableClass pluggableClass;
    private final DeviceProtocol deviceProtocol;

    public SimpleDeviceProtocolPluggableClass(PluggableClass pluggableClass, DeviceProtocol deviceProtocol) {
        this.pluggableClass = pluggableClass;
        this.deviceProtocol = deviceProtocol;
    }

    public PluggableClass getPluggableClass() {
        return pluggableClass;
    }

    public DeviceProtocol getDeviceProtocol() {
        return deviceProtocol;
    }
}
