package com.energyict.protocolimpl.base;

import com.energyict.cpo.AbstractBusinessEvent;
import com.energyict.mdw.core.Device;

/**
 * Copyrights EnergyICT
 * Date: 22/02/11
 * Time: 14:56
 */
public class RtuDiscoveredEvent extends AbstractBusinessEvent {

    /**
     * Creates a new instance of a <code>RtuDiscoveredEvent</code>.
     *
     * @param rtu the <code>Device</code> being discovered.
     */
    public RtuDiscoveredEvent(Device rtu) {
        super(rtu);
    }

    /**
     * Get the new discovered Device that generated this event.
     *
     * @return The new discovered Device
     */
    public Device getSource() {
        return (Device) super.getSource();
    }

    @Override
    public String toString() {
        return "RtuDiscoveredEvent from rtu [" + getSource() + "]";
    }
}
