package com.energyict.genericprotocolimpl.nta.iskra;

import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 12:05:43
 */
public class MbusDevice extends AbstractMbusDevice {
    
    @Override
    public String getVersion() {
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }

}
