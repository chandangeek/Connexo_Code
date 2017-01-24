package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.meterdata.Device;

/**
 * Copyrights EnergyICT
 * Date: 22/02/11
 * Time: 14:56
 */
public class RtuDiscoveredEvent {

    private final Device device;

    /**
     * Creates a new instance of a <code>RtuDiscoveredEvent</code>.
     *
     * @param device the <code>Device</code> being discovered.
     */
    public RtuDiscoveredEvent(Device device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "RtuDiscoveredEvent from device [" + device + "]";
    }

}