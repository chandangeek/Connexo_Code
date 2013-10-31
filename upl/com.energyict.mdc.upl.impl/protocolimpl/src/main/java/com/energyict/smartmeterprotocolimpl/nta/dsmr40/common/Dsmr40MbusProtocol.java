package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 17:22
 */
public class Dsmr40MbusProtocol extends MbusDevice {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT NTA DSMR 4.0 Mbus Slave";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

}
