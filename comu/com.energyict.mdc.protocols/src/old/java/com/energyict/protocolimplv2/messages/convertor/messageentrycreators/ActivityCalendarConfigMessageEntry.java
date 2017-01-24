package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_CAL}
 * xml tag with an additional {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_NAME}
 * and {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_CODE_TABLE}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 12:05
 */
public class ActivityCalendarConfigMessageEntry implements MessageEntryCreator {

    private final String codeTableAttributeName;
    private final String calendarNameAttributeName;

    public ActivityCalendarConfigMessageEntry(String calendarNameAttributeName, String codeTableAttributeName) {
        this.codeTableAttributeName = codeTableAttributeName;
        this.calendarNameAttributeName = calendarNameAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute calendarNameAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, calendarNameAttributeName);
        OfflineDeviceMessageAttribute codeTableAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeTableAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.TOU_ACTIVITY_CAL);
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_ACTIVITY_NAME, calendarNameAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, codeTableAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
