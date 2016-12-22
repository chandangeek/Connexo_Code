package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

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
        int callDistanceHours = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getValue());
        int callDistanceMinutes = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.minute).getValue());
        int callDistanceSeconds = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.second).getValue());

        MessageTag msgTag = new MessageTag(messageTag);

        StringBuilder callDistance = new StringBuilder();
        if (!callDistanceDays.getValue().equals("0")) {
            callDistance.append(callDistanceDays.getValue());
            callDistance.append(" ");
        }
        callDistance.append(String.format("%02d", callDistanceHours));
        callDistance.append(":");
        callDistance.append(String.format("%02d", callDistanceMinutes));
        callDistance.append(":");
        callDistance.append(String.format("%02d", callDistanceSeconds));

        msgTag.add(new MessageAttribute(messageAttribute, callDistance.toString()));
        msgTag.add(new MessageValue(" "));
        return MessageEntry
                    .fromContent(messagingProtocol.writeTag(msgTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }
}
