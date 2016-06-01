package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComMessaging;

import javax.inject.Inject;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 16/09/2014 - 9:49
 */
public class CX20009 extends AM540 {

    @Override
    public String getProtocolDescription() {
        return "Sagemcom CX20009 DLMS (NTA DSMR5.0)";
    }

    @Inject
    public CX20009(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        super(propertySpecService, clock, topologyService, calendarService, ormClient, readingTypeUtilService, loadProfileFactory);
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-10-03 11:24:32 +0200 (vr, 03 okt 2014) $";
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new SagemComMessaging(this, this.getClock(), this.getTopologyService(), this.getCalendarService());
        }
        return messageProtocol;
    }

}