package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a {@link LegacyMessageConverter} for Protocols
 * which are not yet correctly mapped/migrated.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:08
 */
public class NonExistingMessageConverter implements LegacyMessageConverter {

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.noneOf(DeviceMessageId.class);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        throw new UnsupportedOperationException("Formatting messages not allowed on this converter.");
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        throw new UnsupportedOperationException("Executing messages not allowed on this converter.");
    }

    @Override
    public void setMessagingProtocol(Messaging messagingProtocol) {
        // nothing to do
    }

}