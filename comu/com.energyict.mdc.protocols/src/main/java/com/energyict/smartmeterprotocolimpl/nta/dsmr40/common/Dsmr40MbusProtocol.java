package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.mdc.device.topology.TopologyService;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 17:22
 */
public class Dsmr40MbusProtocol extends MbusDevice {

    @Inject
    public Dsmr40MbusProtocol(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT NTA DSMR 4.0 Mbus Slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

}
