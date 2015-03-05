package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy Crypto DSMR4.0 protocols.
 * This is the normal DSMR4.0 message converter, but adds the extra crypto messages.
 *
 * @author khe
 * @since 30/10/13 - 8:33
 */
public class CryptoDsmr40MessageConverter extends Dsmr40MessageConverter {

    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(Dsmr40MessageConverter.registry);

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    static {
        registry.put(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_HLSSECRET, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_AK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY, new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_EK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));

        //Contactor change is (by default) not supported in the crypto protocols
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN);
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
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