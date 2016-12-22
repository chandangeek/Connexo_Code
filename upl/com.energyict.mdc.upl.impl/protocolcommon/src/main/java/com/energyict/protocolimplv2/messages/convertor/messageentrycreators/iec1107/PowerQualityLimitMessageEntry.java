package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Creates a MessageEntry based on the "CLASS_37_UPDATE" xml tag with a value
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class PowerQualityLimitMessageEntry implements MessageEntryCreator {

    private final String powerQualityThresholdAttributeName;

    /**
     * Default constructor
     *
     * @param powerQualityThresholdAttributeName the name of the OfflineDeviceMessageAttribute representing the threshold
     */
    public PowerQualityLimitMessageEntry(String powerQualityThresholdAttributeName) {
        this.powerQualityThresholdAttributeName = powerQualityThresholdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute thresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, powerQualityThresholdAttributeName);
        MessageTag messageTag = new MessageTag("CLASS_37_UPDATE");
        messageTag.add(new MessageValue(thresholdAttribute.getValue()));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
