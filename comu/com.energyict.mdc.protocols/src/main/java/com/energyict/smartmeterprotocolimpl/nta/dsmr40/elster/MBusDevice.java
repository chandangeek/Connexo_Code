package com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.time.Clock;

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
        return "Elster BK-Gx DLMS (NTA DSMR4.0) Mbus Slave";
    }

    @Inject
    public MBusDevice(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient) {
        super(propertySpecService, clock, topologyService, calendarService, readingTypeUtilService, loadProfileFactory, ormClient);
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-02 09:27:19 +0200 (di, 02 apr 2013) $";
    }

}