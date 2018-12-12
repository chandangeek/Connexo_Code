package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Creates a MessageEntry based on the "StartOfDST" xml tag with 4 attributes
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class SetStartOfDSTMessageEntry implements MessageEntryCreator {

    private final String monthAttributeName;
    private final String dayOfMonthAttributeName;
    private final String dayOfWeekAttributeName;
    private final String hourAttributeName;

    /**
     * Default constructor
     */
    public SetStartOfDSTMessageEntry(String monthAttributeName, String dayOfMonthAttributeName, String dayOfWeekAttributeName, String hourAttributeName) {
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
        MessageTag messageTag = new MessageTag("StartOfDST");
        messageTag.add(new MessageAttribute("Month", month.getValue()));
        messageTag.add(new MessageAttribute("Day of month", dayOfMonth.getValue()));
        messageTag.add(new MessageAttribute("Day of week", dayOfWeek.getValue()));
        messageTag.add(new MessageAttribute("Hour", hour.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}