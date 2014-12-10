package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/**
 * Simple test class to correctly perform tests on the adapters
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 9:52
 */
public class SimpleLegacyMessageConverter implements LegacyMessageConverter {

    public static final String codeTableFormattingResult = "ThisIsTheCodeTableFormattingResult";
    public static final String dateFormattingResult = "ThisIsTheDateFormattingResult";
    private final PropertySpecService propertySpecService;

    public SimpleLegacyMessageConverter(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (Code.class.isAssignableFrom(messageAttribute.getClass())) {
            return codeTableFormattingResult;
        } else if (Date.class.isAssignableFrom(messageAttribute.getClass())) {
            return dateFormattingResult;
        }
        return messageAttribute.toString();
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        return new MessageEntry(offlineDeviceMessage.toString(), "");
    }

    @Override
    public void setMessagingProtocol(Messaging messagingProtocol) {
        // nothing to do
    }
}
