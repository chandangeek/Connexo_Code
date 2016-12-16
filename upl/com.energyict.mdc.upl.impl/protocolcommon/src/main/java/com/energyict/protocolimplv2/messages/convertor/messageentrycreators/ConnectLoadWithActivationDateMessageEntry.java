package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

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

    /**
     * Default constructor
     *
     * @param activationDateAttributeName the name of the OfflineDeviceMessageAttribute representing the activationDate
     */
    public ConnectLoadWithActivationDateMessageEntry(String activationDateAttributeName) {
        this.activationDateAttributeName = activationDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_LOAD);
        messageTag.add(new MessageAttribute(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, activationDateAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
