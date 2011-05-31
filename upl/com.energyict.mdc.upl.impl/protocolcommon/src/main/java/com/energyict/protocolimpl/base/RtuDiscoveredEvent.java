package com.energyict.protocolimpl.base;

import com.energyict.cpo.AbstractBusinessEvent;
import com.energyict.mdw.core.Rtu;

/**
 * Copyrights EnergyICT
 * Date: 22/02/11
 * Time: 14:56
 */
public class RtuDiscoveredEvent extends AbstractBusinessEvent {

    /**
     * Creates a new instance of a <code>RtuDiscoveredEvent</code>.
     *
     * @param rtu the <code>Rtu</code> being discovered.
     */
    public RtuDiscoveredEvent(Rtu rtu) {
        super(rtu);
    }

    /**
     * Get the new discovered Rtu that generated this event.
     *
     * @return The new discovered Rtu
     */
    public Rtu getSource() {
        return (Rtu) super.getSource();
    }

    @Override
    public String toString() {
        return "RtuDiscoveredEvent from rtu [" + getSource() + "]";
    }
}
