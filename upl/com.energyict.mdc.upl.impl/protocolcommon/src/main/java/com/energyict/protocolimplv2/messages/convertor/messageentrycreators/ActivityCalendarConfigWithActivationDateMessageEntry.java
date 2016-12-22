package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_CAL}
 * xml tag with an additional {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_NAME},
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_CODE_TABLE} and
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_ACTIVITY_DATE}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 13:20
 */
public class ActivityCalendarConfigWithActivationDateMessageEntry implements MessageEntryCreator {

    private final String codeTableAttributeName;
    private final String calendarNameAttributeName;
    private final String activationDateAttributeName;

    public ActivityCalendarConfigWithActivationDateMessageEntry(String calendarNameAttributeName, String codeTableAttributeName, String activationDateAttributeName) {
        this.codeTableAttributeName = codeTableAttributeName;
        this.calendarNameAttributeName = calendarNameAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute calendarNameAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, calendarNameAttributeName);
        OfflineDeviceMessageAttribute codeTableAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeTableAttributeName);
        OfflineDeviceMessageAttribute activationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.TOU_ACTIVITY_CAL);
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_ACTIVITY_NAME, calendarNameAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_ACTIVITY_DATE, activationDateAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, codeTableAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
