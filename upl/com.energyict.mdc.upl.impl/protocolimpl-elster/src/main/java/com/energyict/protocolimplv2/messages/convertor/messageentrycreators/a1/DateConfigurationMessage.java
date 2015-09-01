package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * @author sva
 * @since 14/08/2015 - 10:17
 */
public class DateConfigurationMessage implements MessageEntryCreator {

    private final String messageTag;
    private final String messageAttribute;

    public DateConfigurationMessage(String messageTag, String messageAttribute) {
        this.messageTag = messageTag;
        this.messageAttribute = messageAttribute;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute callDistanceDays = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.day);
        int callDistanceHours = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getDeviceMessageAttributeValue());
        int callDistanceMinutes = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.minute).getDeviceMessageAttributeValue());
        int callDistanceSeconds = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.second).getDeviceMessageAttributeValue());

        MessageTag msgTag = new MessageTag(messageTag);

        StringBuilder callDistance = new StringBuilder();
        if (!callDistanceDays.getDeviceMessageAttributeValue().equals("0")) {
            callDistance.append(callDistanceDays.getDeviceMessageAttributeValue());
            callDistance.append(" ");
        }
        callDistance.append(String.format("%02d", callDistanceHours));
        callDistance.append(":");
        callDistance.append(String.format("%02d", callDistanceMinutes));
        callDistance.append(":");
        callDistance.append(String.format("%02d", callDistanceSeconds));

        msgTag.add(new MessageAttribute(messageAttribute, callDistance.toString()));
        msgTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(msgTag), offlineDeviceMessage.getTrackingId());
    }
}
