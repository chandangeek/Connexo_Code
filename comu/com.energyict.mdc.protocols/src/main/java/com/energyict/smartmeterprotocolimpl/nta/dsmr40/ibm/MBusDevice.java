/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import javax.inject.Inject;
import java.time.Clock;

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