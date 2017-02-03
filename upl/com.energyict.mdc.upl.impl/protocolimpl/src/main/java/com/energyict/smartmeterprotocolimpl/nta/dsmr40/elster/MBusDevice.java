package com.energyict.smartmeterprotocolimpl.nta.dsmr40.elster;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Protocol for the Elster g-meter, following the DSMR 4.0 spec.
 * This meter should behave exactly the same as the L+G DSMR 4.0 g-meter
 * <p>
 * Copyrights EnergyICT
 * Date: 2/04/13
 * Time: 9:24
 * Author: khe
 */
public class MBusDevice extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.MBusDevice {

    public MBusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public String getVersion() {
        return "$Date: Mon Jun 2 11:26:25 2014 +0000 $";
    }
}
