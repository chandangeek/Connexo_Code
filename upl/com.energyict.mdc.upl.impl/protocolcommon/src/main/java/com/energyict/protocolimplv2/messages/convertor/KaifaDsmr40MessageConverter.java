package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 7/01/2015 - 15:24
 */
public class KaifaDsmr40MessageConverter extends Dsmr40MessageConverter {

    public KaifaDsmr40MessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, NumberLookupExtractor numberLookupExtractor, TariffCalendarExtractor calendarExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, calendarExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.put(messageSpec(MBusSetupDeviceMessage.Reset_MBus_Client), new MultipleAttributeMessageEntry(RtuMessageConstant.RESET_MBUS_CLIENT, RtuMessageConstant.MBUS_SERIAL_NUMBER));
        return registry;
    }

}