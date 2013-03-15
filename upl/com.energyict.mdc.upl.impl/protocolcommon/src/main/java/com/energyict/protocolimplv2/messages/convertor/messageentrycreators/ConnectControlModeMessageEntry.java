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
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#CONNECT_CONTROL_MODE}
 * xml tag with an additional
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#CONNECT_MODE} attribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class ConnectControlModeMessageEntry implements MessageEntryCreator {

    private final String connectModeAttributeName;

    /**
     * Default constructor
     *
     * @param connectModeAttributeName the name of the OfflineDeviceMessageAttribute representing the connect mode
     */
    public ConnectControlModeMessageEntry(String connectModeAttributeName) {
        this.connectModeAttributeName = connectModeAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute connectModeAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, connectModeAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_CONTROL_MODE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.CONNECT_MODE, connectModeAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
