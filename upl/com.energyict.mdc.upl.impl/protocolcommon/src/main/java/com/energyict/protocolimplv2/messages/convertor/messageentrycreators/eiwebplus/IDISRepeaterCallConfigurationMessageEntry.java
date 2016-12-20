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
public class IDISRepeaterCallConfigurationMessageEntry implements MessageEntryCreator {

    public static final String OPEN_TAG = "<IDISRepeaterCallConfiguration>";
    public static final String CLOSE_TAG = "</IDISRepeaterCallConfiguration>";
    public static final String OPEN_SUBTAG1 = "<Interval (in minutes)>";
    public static final String CLOSE_SUBTAG1 = "</Interval (in minutes)>";
    public static final String OPEN_SUBTAG2 = "<Reception threshold (dBV)>";
    public static final String CLOSE_SUBTAG2 = "</Reception threshold (dBV)>";
    public static final String OPEN_SUBTAG3 = "<Number of timeslots for NEW systems>";
    public static final String CLOSE_SUBTAG3 = "</Number of timeslots for NEW systems>";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String interval = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.interval).getValue();
        String duration = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.receptionThreshold).getValue();
        String numberOfTimeSlots = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.numberOfTimeSlotsForNewSystems).getValue();
        String messageContent = OPEN_TAG + OPEN_SUBTAG1 + interval + CLOSE_SUBTAG1 + OPEN_SUBTAG2 + duration + CLOSE_SUBTAG2 + OPEN_SUBTAG3 + numberOfTimeSlots + CLOSE_SUBTAG3 + CLOSE_TAG;
        return new MessageEntry(messageContent, offlineDeviceMessage.getTrackingId());
    }
}