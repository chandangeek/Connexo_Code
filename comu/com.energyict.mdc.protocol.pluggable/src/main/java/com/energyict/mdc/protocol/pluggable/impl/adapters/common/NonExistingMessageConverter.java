package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import java.util.Collections;
import java.util.List;

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
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
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