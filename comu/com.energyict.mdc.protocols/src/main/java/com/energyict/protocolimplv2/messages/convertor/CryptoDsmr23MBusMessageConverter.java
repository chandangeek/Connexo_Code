package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.common.HexString;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

/**
 * Represents a MessageConverter for the legacy Crypto DSM2.3 MBusDevice protocols.
 * This is the normal DSMR2.3 MBusDevice message converter, but adds the extra crypto message.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr23MBusMessageConverter extends Dsmr23MBusDeviceMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link MessageEntryCreator}
     */
    static {
        registry.put(MBusSetupDeviceMessage.SetEncryptionKeysUsingCryptoserver, new MultipleAttributeMessageEntry(RtuMessageConstant.CRYPTOSERVER_MBUS_ENCRYPTION_KEYS, RtuMessageConstant.MBUS_DEFAULT_KEY));
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