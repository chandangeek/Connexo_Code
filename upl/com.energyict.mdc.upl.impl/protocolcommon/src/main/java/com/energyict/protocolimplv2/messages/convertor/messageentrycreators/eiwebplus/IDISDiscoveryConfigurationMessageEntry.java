package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class IDISDiscoveryConfigurationMessageEntry implements MessageEntryCreator {

    public static final String OPEN_TAG = "<IDISDiscoveryConfiguration>";
    public static final String CLOSE_TAG = "</IDISDiscoveryConfiguration>";
    public static final String OPEN_SUBTAG1 = "<Interval between discoveries (in hours)>";
    public static final String CLOSE_SUBTAG1 = "</Interval between discoveries (in hours)>";
    public static final String OPEN_SUBTAG2 = "<Duration of the discovery (in minutes)>";
    public static final String CLOSE_SUBTAG2 = "</Duration of the discovery (in minutes)>";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String interval = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.interval).getDeviceMessageAttributeValue();
        String duration = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.duration).getDeviceMessageAttributeValue();
        String messageContent = OPEN_TAG + OPEN_SUBTAG1 + interval + CLOSE_SUBTAG1 + OPEN_SUBTAG2 + duration + CLOSE_SUBTAG2 + CLOSE_TAG;
        return new MessageEntry(messageContent, offlineDeviceMessage.getTrackingId());
    }
}