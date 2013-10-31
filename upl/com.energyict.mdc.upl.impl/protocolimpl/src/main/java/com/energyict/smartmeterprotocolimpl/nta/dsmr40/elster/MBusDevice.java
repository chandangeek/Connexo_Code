package com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster;

/**
 * Protocol for the Elster g-meter, following the DSMR 4.0 spec.
 * This meter should behave exactly the same as the L+G DSMR 4.0 g-meter
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 9:24
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice {

    @Override
    public String getProtocolDescription() {
        return "Elster Mbus Slave NTA DSM 4.0";
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-02 09:27:19 +0200 (di, 02 apr 2013) $";
    }
}
