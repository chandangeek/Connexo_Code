package com.energyict.genericprotocolimpl.nta.eict;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 11:59:15
 */
public class MbusDevice extends com.energyict.genericprotocolimpl.webrtukp.MbusDevice {

    @Override
    public String getVersion() {
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }

}
