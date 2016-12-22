package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy Crypto DSM2.3 MBusDevice protocols.
 * This is the normal DSMR2.3 MBusDevice message converter, but adds the extra crypto message.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr23MBusMessageConverter extends Dsmr23MBusDeviceMessageConverter {

    public CryptoDsmr23MBusMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.put(messageSpec(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver), new MultipleAttributeMessageEntry(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS, RtuMessageConstant.MBUS_DEFAULT_KEY));

        // Contactor change is messageSpec)by default)) not supported in the crypto protocol
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.defaultKeyAttributeName:
                return ((HexString) messageAttribute).getContent();
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }
}