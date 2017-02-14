package com.energyict.smartmeterprotocolimpl.nta.dsmr23.iskra;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractNtaMbusDevice;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 12:04:42
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    public MbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr23MbusMessaging();
    }

    @Override
    public String getVersion() {
        return "$Date: Mon Jan 2 11:14:35 2017 +0100 $";
    }

    @Override
    public void setUPLProperties(TypedProperties properties) {
    }

}