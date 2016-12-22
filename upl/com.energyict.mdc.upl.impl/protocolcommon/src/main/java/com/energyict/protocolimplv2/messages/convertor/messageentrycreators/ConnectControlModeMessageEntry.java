package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

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
        messageTag.add(new MessageAttribute(RtuMessageConstant.CONNECT_MODE, connectModeAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
