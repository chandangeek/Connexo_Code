package com.energyict.common;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.util.Arrays;

public class CommonCryptoMessaging {
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public CommonCryptoMessaging(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    public String format(OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return ((HexString) messageAttribute).getContent();
            case DeviceMessageConstants.newAuthenticationKeyAttributeName:
            case DeviceMessageConstants.newEncryptionKeyAttributeName:
                if (Arrays.<DeviceMessageSpec>asList(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.get(propertySpecService, nlsService, converter))
                        .contains(offlineDeviceMessage.getSpecification())) {
                    return ((HexString) messageAttribute).getContent();
                }
        }
        return null;
    }

    public String prepareMessageContext(Device device, DeviceMessage deviceMessage) {
        if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.id()) {
            new CryptoKeyMessageChangeValidator().validateNewKeyValue(device, deviceMessage, SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY);
        } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.id()) {
            new CryptoKeyMessageChangeValidator().validateNewKeyValue(device, deviceMessage, SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY);
        } else if (deviceMessage.getMessageId() == SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEY.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEY_FOR_CLIENT.id()
                || deviceMessage.getMessageId() == SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEY_FOR_PREDEFINED_CLIENT.id()) {
            new CryptoKeyMessageChangeValidator().validateNewKeyValue(device, deviceMessage, SecurityPropertySpecTranslationKeys.MASTER_KEY);
        }
        return "";
    }
}
