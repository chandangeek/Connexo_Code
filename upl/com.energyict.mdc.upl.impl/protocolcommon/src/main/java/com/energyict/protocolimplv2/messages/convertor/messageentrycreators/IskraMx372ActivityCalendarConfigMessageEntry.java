package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry for tariff programming
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 12:05
 */
public class IskraMx372ActivityCalendarConfigMessageEntry implements MessageEntryCreator {

    private final String codeTableAttributeName;
    private final String calendarNameAttributeName;

    public IskraMx372ActivityCalendarConfigMessageEntry(String calendarNameAttributeName, String codeTableAttributeName) {
        this.codeTableAttributeName = codeTableAttributeName;
        this.calendarNameAttributeName = calendarNameAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute calendarNameAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, calendarNameAttributeName);
        OfflineDeviceMessageAttribute codeTableAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeTableAttributeName);

        MessageTag messageTag = new MessageTag("UserFile ID of tariff program");
        messageTag.add(new MessageValue(codeTableAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
