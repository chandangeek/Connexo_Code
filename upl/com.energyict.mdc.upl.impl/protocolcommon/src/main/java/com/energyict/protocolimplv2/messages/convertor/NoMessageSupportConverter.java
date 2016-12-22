package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.Collections;
import java.util.List;

/**
 * Represents a MessageConverter for protocols which don't have any messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 14:07
 */
public class NoMessageSupportConverter implements LegacyMessageConverter {

    public NoMessageSupportConverter() {
        super();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        return MessageEntry.empty();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

}