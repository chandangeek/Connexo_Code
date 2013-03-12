package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.util.Arrays;
import java.util.List;

/**
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#CONNECT_LOAD}
 * xml tag with an additional
 * {@link RtuMessageConstant#DISCONNECT_CONTROL_ACTIVATE_DATE} attribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:43
 */
public class ConnectLoadWithActivationDateMessageEntry implements MessageEntryCreator {

    private final String activationDateAttributeName;
    private final List<MessageValueSpec> messageValueSpecs;

    /**
     * Default constructor
     *
     * @param activationDateAttributeName the name of the OfflineDeviceMessageAttribute representing the activationDate
     */
    public ConnectLoadWithActivationDateMessageEntry(String activationDateAttributeName, MessageValueSpec... messageValueSpecs) {
        this.activationDateAttributeName = activationDateAttributeName;
        this.messageValueSpecs = Arrays.asList(messageValueSpecs);
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_LOAD);
        messageTag.add(new MessageAttribute(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, activationDateAttribute.getDeviceMessageAttributeValue()));
        for (MessageValueSpec messageValueSpec : messageValueSpecs) {
            messageTag.add(new MessageValue(messageValueSpec.getValue()));
        }
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
