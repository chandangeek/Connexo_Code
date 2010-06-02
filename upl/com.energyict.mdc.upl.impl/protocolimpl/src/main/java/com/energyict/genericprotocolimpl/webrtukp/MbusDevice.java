package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;

/**
 * Copyrights EnergyICT
 * Date: 2-jun-2010
 * Time: 16:37:17
 *
 * @deprecated use the {@link com.energyict.genericprotocolimpl.nta.eict.MbusDevice} instead
 */
public class MbusDevice extends AbstractMbusDevice {

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
