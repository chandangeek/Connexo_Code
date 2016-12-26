package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Represents a MessageConverter for the legacy Xemex ReMI MBusDevice protocol.
 *
 *  @author sva
  * @since 30/10/13 - 12:21
 */
public class XemexMBusDeviceMessageConverter extends AbstractMessageConverter {

    private final Extractor extractor;

    public XemexMBusDeviceMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.extractor = extractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap.of(
                // LoadProfiles
                messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName),
                messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
            	return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.extractor);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            	return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }
}
