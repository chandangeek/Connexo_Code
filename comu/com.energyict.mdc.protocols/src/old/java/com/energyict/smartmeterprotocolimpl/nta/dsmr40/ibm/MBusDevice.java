package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;

import javax.inject.Inject;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 16:18
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice {

    @Override
    public String getProtocolDescription() {
        return "IBM Kaifa DLMS (NTA DSMR4.0) Mbus Slave";
    }

    @Inject
    public MBusDevice(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient) {
        super(propertySpecService, clock, topologyService, calendarService, readingTypeUtilService, loadProfileFactory, ormClient);
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-03-12 16:42:27 +0100 (di, 12 mrt 2013) $";
    }

}