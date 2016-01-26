package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice;

import javax.inject.Inject;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 17:22
 */
@Deprecated //Never released, technical class
public class Dsmr40MbusProtocol extends MbusDevice {

    @Override
    public String getProtocolDescription() {
        return "DSMR40 MBus slave technical class";
    }

    @Inject
    public Dsmr40MbusProtocol(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient) {
        super(propertySpecService, clock, topologyService, readingTypeUtilService, loadProfileFactory, ormClient);
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

}