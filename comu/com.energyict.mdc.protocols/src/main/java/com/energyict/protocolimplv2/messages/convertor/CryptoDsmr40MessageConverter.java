package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

/**
 * Represents a MessageConverter for the legacy Crypto DSMR4.0 protocols.
 * This is the normal DSMR4.0 message converter, but adds the extra crypto messages.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr40MessageConverter extends Dsmr40MessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link MessageEntryCreator}
     */
    static {
        registry.put(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_HLSSECRET, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_AK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_EK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
    }

    public CryptoDsmr40MessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return ((HexString) messageAttribute).getContent();
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }
}