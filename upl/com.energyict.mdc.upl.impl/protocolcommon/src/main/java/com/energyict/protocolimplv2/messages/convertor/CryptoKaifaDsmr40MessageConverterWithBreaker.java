package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
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
public class CryptoKaifaDsmr40MessageConverterWithBreaker extends CryptoKaifaDsmr40MessageConverter {

    public CryptoKaifaDsmr40MessageConverterWithBreaker(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
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