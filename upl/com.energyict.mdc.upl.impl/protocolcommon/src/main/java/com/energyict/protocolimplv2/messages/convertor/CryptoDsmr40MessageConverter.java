package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

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

    public CryptoDsmr40MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, NumberLookupExtractor numberLookupExtractor, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, loadProfileExtractor, numberLookupExtractor, messageFileExtractor, calendarExtractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.put(messageSpec(SecurityMessage.CHANGE_HLS_SECRET_USING_SERVICE_KEY), new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_HLSSECRET, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(messageSpec(SecurityMessage.CHANGE_AUTHENTICATION_KEY_USING_SERVICE_KEY), new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_AK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));
        registry.put(messageSpec(SecurityMessage.CHANGE_ENCRYPTION_KEY_USING_SERVICE_KEY), new MultipleAttributeMessageEntry(RtuMessageConstant.SERVICEKEY_EK, RtuMessageConstant.SERVICEKEY_PREPAREDDATA, RtuMessageConstant.SERVICEKEY_SIGNATURE, RtuMessageConstant.SERVICEKEY_VERIFYKEY));

        //Contactor change is (by default) not supported in the crypto protocols
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
            case DeviceMessageConstants.preparedDataAttributeName:
            case DeviceMessageConstants.signatureAttributeName:
                return ((HexString) messageAttribute).getContent();
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

}