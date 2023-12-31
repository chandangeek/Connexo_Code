package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ek280;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * @author sva
 * @since 1/09/2015 - 11:27
 */
public class ConfigureAutoConnectModeMessageEntry implements MessageEntryCreator {

    private static final String NOT_APPLICABLE_TEXT = "N/A";
    private final String messageTag = "SetAutoConnect";
    private final String windowAttributeTag = "AutoConnectId";
    private final String modeAttributeTag = "AutoConnectMode";
    private final String startTimeAttributeTag = "AutoConnectStart";
    private final String endTimeAttributeTag = "AutoConnectEnd";
    private final String destination1AttributeTag = "Destination1";
    private final String destination2AttributeTag = "Destination2";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String autoConnectWindow = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.windowAttributeName).getValue();
        String autoConnectMode = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.autoConnectMode).getValue();
        String autoConnectStartTime = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.autoConnectStartTime).getValue();
        String autoConnectEndTime = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.autoConnectEndTime).getValue();
        String autoConnectDestination1 = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.autoConnectDestination1).getValue();
        String autoConnectDestionation2 = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.autoConnectDestination2).getValue();

        MessageTag msgTag = new MessageTag(messageTag);
        msgTag.add(new MessageAttribute(windowAttributeTag, autoConnectWindow));
        msgTag.add(new MessageAttribute(modeAttributeTag, Integer.toString(NetworkConnectivityMessage.AutoConnectMode.modeForDescription(autoConnectMode).getMode())));
        msgTag.add(new MessageAttribute(startTimeAttributeTag, autoConnectStartTime));
        if (!autoConnectEndTime.equals(NOT_APPLICABLE_TEXT)) {
            msgTag.add(new MessageAttribute(endTimeAttributeTag, autoConnectEndTime));
        }
        msgTag.add(new MessageAttribute(destination1AttributeTag, autoConnectDestination1));
        if (!autoConnectDestionation2.equals(NOT_APPLICABLE_TEXT)) {
            msgTag.add(new MessageAttribute(destination2AttributeTag, autoConnectDestionation2));
        }
        msgTag.add(new MessageValue(" "));
        return MessageEntry
                    .fromContent(messagingProtocol.writeTag(msgTag))
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }
}
