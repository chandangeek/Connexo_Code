package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a MessageEntry based on the {@link RtuMessageConstant#CONNECT_LOAD}
 * xml tag with no additional parameters
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:33
 */
public class ConnectLoadMessageEntry implements MessageEntryCreator {

    private final List<MessageValueSpec> messageValueSpecs;

    public ConnectLoadMessageEntry(MessageValueSpec... messageValueSpecs) {
        this.messageValueSpecs = Arrays.asList(messageValueSpecs);
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_LOAD);
        for (MessageValueSpec messageValueSpec : messageValueSpecs) {
            messageTag.add(new MessageValue(messageValueSpec.getValue()));
        }
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
