package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.AbstractBusinessEvent;
import com.energyict.mdc.protocol.api.device.Device;

/**
 * Copyrights EnergyICT
 * Date: 22/02/11
 * Time: 14:56
 */
public class RtuDiscoveredEvent extends AbstractBusinessEvent<Device> {

    /**
     * Creates a new instance of a <code>RtuDiscoveredEvent</code>.
     *
     * @param rtu the <code>Device</code> being discovered.
     */
    public RtuDiscoveredEvent(Device rtu) {
        super(rtu);
    }

    @Override
    public String toString() {
        return "RtuDiscoveredEvent from rtu [" + getSource() + "]";
    }

}