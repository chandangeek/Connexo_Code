package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:18
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice {

    @Inject
    public MBusDevice(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    public String getProtocolDescription() {
        return "IBM Mbus Slave NTA DSMR 4.0";
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-03-12 16:42:27 +0100 (di, 12 mrt 2013) $";
    }

}