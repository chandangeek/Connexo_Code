package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class ForceMessageToFailedMessageEntry implements MessageEntryCreator {

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String deviceId = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.deviceId).getValue();
        String trackingID = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.trackingId).getValue();
        String messageContent = "<ForceMessageToFailed><RtuID>" + deviceId + "</RtuID><TrackingID>" + trackingID + "</TrackingID> </ForceMessageToFailed>";
        return MessageEntry
                    .fromContent(messageContent)
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }
}