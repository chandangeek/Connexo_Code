package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;

import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;

import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 10/04/13
 * Time: 16:14
 * Author: khe
 */
public class KaifaDsmr40MessageExecutor extends Dsmr40MessageExecutor {

    public KaifaDsmr40MessageExecutor(AbstractSmartNtaProtocol protocol, Clock clock, TopologyService topologyService, CalendarService calendarService) {
        super(protocol, clock, topologyService, calendarService);
    }

    /**
     * The IBM Kaifa meter only accepts value 0x01 as boolean TRUE.
     */
    protected int getBooleanValue() {
        return 0x01;
    }

}