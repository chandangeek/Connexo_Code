package com.energyict.common;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.util.Arrays;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.keyAccessorTypeAttributeName;

public class CommonCryptoMessaging {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public CommonCryptoMessaging(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    public String format(OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return ((HexString) messageAttribute).getContent();
            case DeviceMessageConstants.newAuthenticationKeyAttributeName:
            case DeviceMessageConstants.newEncryptionKeyAttributeName:
            case DeviceMessageConstants.newMasterKeyAttributeName:
            case DeviceMessageConstants.newPasswordAttributeName:
            case DeviceMessageConstants.newPSKAttributeName:
            case DeviceMessageConstants.newPSKKEKAttributeName:
                if (Arrays.asList(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_PREDEFINED_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.get(propertySpecService, nlsService, converter),
                        SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY_AND_NEW_PLAIN_KEY_FOR_CLIENT.get(propertySpecService, nlsService, converter))
                        .contains(offlineDeviceMessage.getSpecification())) {
                    return ((HexString) messageAttribute).getContent();
                } else {
                    return keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
                }
            case keyAccessorTypeAttributeName:
                return keyAccessorTypeExtractor.name((KeyAccessorType) messageAttribute) + CommonCryptoMessageExecutor.SEPARATOR +
                       keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
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
