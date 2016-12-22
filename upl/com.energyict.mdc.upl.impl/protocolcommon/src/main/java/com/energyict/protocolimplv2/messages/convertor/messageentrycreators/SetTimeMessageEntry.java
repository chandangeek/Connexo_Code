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
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#SET_TIME}
 * xml tag with an additional
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#SET_TIME_VALUE} attribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:49
 */
public class SetTimeMessageEntry implements MessageEntryCreator {

    private final String dateAttributeName;

    /**
     * Default constructor
     *
     * @param dateAttributeName the name of the OfflineDeviceMessageAttribute representing the time to set
     */
    public SetTimeMessageEntry(String dateAttributeName) {
        this.dateAttributeName = dateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute dateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.SET_TIME);
        messageTag.add(new MessageAttribute(RtuMessageConstant.SET_TIME_VALUE, dateAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}