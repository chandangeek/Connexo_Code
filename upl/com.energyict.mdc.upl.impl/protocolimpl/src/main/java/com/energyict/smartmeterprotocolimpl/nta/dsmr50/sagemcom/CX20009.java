package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages.SagemComMessaging;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 16/09/2014 - 9:49
 */
public class CX20009 extends AM540 {

    private final TariffCalendarFinder calendarFinder;
    private final Extractor extractor;

    public CX20009(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, Extractor extractor) {
        super(propertySpecService, extractor, calendarFinder);
        this.calendarFinder = calendarFinder;
        this.extractor = extractor;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-10-03 11:24:32 +0200 (vr, 03 okt 2014) $";
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new SagemComMessaging(this, this.calendarFinder, this.extractor);
        }
        return messageProtocol;
    }

}