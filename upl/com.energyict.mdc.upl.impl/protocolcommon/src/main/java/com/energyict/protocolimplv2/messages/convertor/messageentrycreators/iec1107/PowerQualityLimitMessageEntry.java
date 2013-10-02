package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.iec1107;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

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
        messageTag.add(new MessageValue(thresholdAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
