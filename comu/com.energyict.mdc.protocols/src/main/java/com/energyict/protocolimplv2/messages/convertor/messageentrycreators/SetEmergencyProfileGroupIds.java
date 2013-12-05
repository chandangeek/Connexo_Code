package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.device.data.MessageEntry;
import com.energyict.mdc.protocol.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 11:24
 */
public class SetEmergencyProfileGroupIds implements MessageEntryCreator {

    private final String emergencyProfileIdLookupAttributeName;

    public SetEmergencyProfileGroupIds(String emergencyProfileIdLookupAttributeName) {
        this.emergencyProfileIdLookupAttributeName = emergencyProfileIdLookupAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute emergencyProfileLookupAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileIdLookupAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
        messageTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_EP_GRID_LOOKUP_ID, emergencyProfileLookupAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
