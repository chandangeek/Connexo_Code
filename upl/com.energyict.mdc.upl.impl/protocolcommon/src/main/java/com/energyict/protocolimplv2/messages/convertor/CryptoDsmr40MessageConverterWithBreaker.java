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

import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectControlModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadWithActivationDateMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadWithActivationDateMessageEntry;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;

/**
 * Extension of the Crypto DSMR2.3 message converter, that adds the breaker messages again.
 *
 * @author khe
 */
public class CryptoDsmr40MessageConverterWithBreaker extends CryptoDsmr40MessageConverter {

    public CryptoDsmr40MessageConverterWithBreaker(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, NumberLookupExtractor numberLookupExtractor, TariffCalendarExtractor calendarExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, calendarExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry());
        registry.put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE), new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry());
        registry.put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE), new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new ConnectControlModeMessageEntry(contactorModeAttributeName));
        return registry;
    }

}