package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 17:22
 */
@Deprecated //Never released, technical class
public class Dsmr40MbusProtocol extends MbusDevice {

    @Override
    public String getVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

}
