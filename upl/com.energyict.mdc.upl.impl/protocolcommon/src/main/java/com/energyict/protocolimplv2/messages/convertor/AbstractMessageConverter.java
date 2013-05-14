package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 15:05
 */
public abstract class AbstractMessageConverter implements LegacyMessageConverter {

    private Messaging messagingProtocol;

    /**
     * Get the registry which contains the mapping between the DeviceMessageSpecs
     * and the MessageEntryCreators.
     *
     * @return the registry mapping
     */
    protected abstract Map<DeviceMessageSpec, MessageEntryCreator> getRegistry();

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>(getRegistry().keySet());
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        final DeviceMessageSpec deviceMessageSpec = MessageConverterTools.getDeviceMessageSpecForOfflineDeviceMessage(offlineDeviceMessage);

        final MessageEntryCreator messageEntryCreator = getRegistry().get(deviceMessageSpec);
        if (messageEntryCreator != null) {
            return messageEntryCreator.createMessageEntry(getMessagingProtocol(), offlineDeviceMessage);
        } else {
            return new MessageEntry("", "");
        }
    }

    @Override
    public void setMessagingProtocol(Messaging messaging) {
        this.messagingProtocol = messaging;
    }

    protected Messaging getMessagingProtocol() {
        return this.messagingProtocol;
    }
}
