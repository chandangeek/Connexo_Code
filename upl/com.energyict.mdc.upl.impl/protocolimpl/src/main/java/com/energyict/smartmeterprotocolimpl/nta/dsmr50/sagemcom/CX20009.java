package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
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

    public CX20009(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: Fri Oct 3 14:53:35 2014 +0000 $";
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new SagemComMessaging(this, this.getCalendarFinder(), this.getCalendarExtractor(), this.getMessageFileFinder(), getMessageFileExtractor(), getNumberLookupFinder(), getNumberLookupExtractor());
        }
        return messageProtocol;
    }

}