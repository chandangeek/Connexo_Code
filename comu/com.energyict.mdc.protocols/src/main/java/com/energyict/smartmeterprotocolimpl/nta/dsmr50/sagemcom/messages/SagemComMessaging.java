package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;

import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor;

import java.time.Clock;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/10/2014 - 9:17
 */
public class SagemComMessaging extends AM540Messaging {

    public SagemComMessaging(AM540 protocol, Clock clock, TopologyService topologyService, CalendarService calendarService) {
        super(protocol, topologyService, clock, calendarService);
    }

    protected Dsmr50MessageExecutor getMessageExecutor() {
        return new SagemComDsmr50MessageExecutor(protocol, this.getClock(), this.getTopologyService(), this.getCalendarService());
    }

}