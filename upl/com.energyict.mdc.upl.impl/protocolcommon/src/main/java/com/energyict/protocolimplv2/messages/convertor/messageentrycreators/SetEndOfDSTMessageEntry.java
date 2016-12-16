package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the "EndOfDST" xml tag with 4 attributes
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class SetEndOfDSTMessageEntry implements MessageEntryCreator {

    private final String monthAttributeName;
    private final String dayOfMonthAttributeName;
    private final String dayOfWeekAttributeName;
    private final String hourAttributeName;

    /**
     * Default constructor
     */
    public SetEndOfDSTMessageEntry(String monthAttributeName, String dayOfMonthAttributeName, String dayOfWeekAttributeName, String hourAttributeName) {
        this.monthAttributeName = monthAttributeName;
        this.dayOfMonthAttributeName = dayOfMonthAttributeName;
        this.dayOfWeekAttributeName = dayOfWeekAttributeName;
        this.hourAttributeName = hourAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute month = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, monthAttributeName);
        OfflineDeviceMessageAttribute dayOfMonth = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dayOfMonthAttributeName);
        OfflineDeviceMessageAttribute dayOfWeek = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, dayOfWeekAttributeName);
        OfflineDeviceMessageAttribute hour = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, hourAttributeName);
        MessageTag messageTag = new MessageTag("EndOfDST");
        messageTag.add(new MessageAttribute("Month", month.getValue()));
        messageTag.add(new MessageAttribute("Day of month", dayOfMonth.getValue()));
        messageTag.add(new MessageAttribute("Day of week", dayOfWeek.getValue()));
        messageTag.add(new MessageAttribute("Hour", hour.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}