package com.energyict.smartmeterprotocolimpl.nta.esmr50.elster;


import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Protocol;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.elster.events.ElsterEsmr50EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.elster.registers.AS3300TRegisterFactory;
@Deprecated
public class AS3300T extends ESMR50Protocol {

    protected AS3300T(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new AS3300TRegisterFactory(this);
        }
        return this.registerFactory;
    }

    /**
     * Getter for the <b>ESMR 5.0</b> EventProfile
     *
     * @return the lazy loaded EventProfile
     */
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new ElsterEsmr50EventProfile(this);
        }
        return eventProfile;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new ESMR50Messaging(new ESMR50MessageExecutor(this, this.getCalendarFinder(), this.getCalendarExtractor(),this.getMessageFileFinder(), this.getMessageFileExtractor(), this.getNumberLookupExtractor(), this.getNumberLookupFinder()));
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-04-07 12:11:18 +0300 (Thu, 07 Apr 2016) $";
    }

    @Override
    public String getProtocolDescription() {
        return "AS3300T protocol";
    }
}
