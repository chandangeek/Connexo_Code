package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
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

    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(Dsmr23MBusDeviceMessageConverter.registry);  //Clone the messages of the super class

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    static {
        registry.put(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver, new MultipleAttributeMessageEntry(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS, RtuMessageConstant.MBUS_DEFAULT_KEY));

        //Contactor change is (by default) not supported in the crypto protocols
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN);
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
    }

    public CryptoDsmr23MBusMessageConverter() {
        super();
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