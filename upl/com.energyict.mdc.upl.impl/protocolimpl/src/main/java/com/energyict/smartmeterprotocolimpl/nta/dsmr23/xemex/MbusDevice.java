package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.XemexWatchTalkMbusMessaging;

/**
 * @author sva
 * @since 20/03/2014 - 11:58
 */
public class MbusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.MbusDevice {

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexWatchTalkMbusMessaging();
    }

    @Override
    public String getVersion() {
        return "$Date: Thu Nov 26 10:45:14 2015 +0100 $";
    }
}
