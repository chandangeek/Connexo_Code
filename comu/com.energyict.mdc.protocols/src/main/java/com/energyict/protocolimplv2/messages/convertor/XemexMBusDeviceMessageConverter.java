package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;

/**
 * Represents a MessageConverter for the legacy Xemex ReMI MBusDevice protocol.
 *
 *  @author sva
  * @since 30/10/13 - 12:21
 */
public class XemexMBusDeviceMessageConverter extends AbstractMessageConverter {

    public XemexMBusDeviceMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // LoadProfiles
        registry.put(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
            	return LoadProfileMessageUtils.formatLoadProfile((BaseLoadProfile) messageAttribute);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            	return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }
}
