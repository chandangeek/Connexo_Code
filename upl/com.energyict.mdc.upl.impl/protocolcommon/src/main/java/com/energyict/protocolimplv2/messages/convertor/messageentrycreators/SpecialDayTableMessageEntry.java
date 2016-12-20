package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_SPECIAL_DAYS}
 * xml tag with an additional {@link com.energyict.protocolimpl.messages.RtuMessageConstant#TOU_SPECIAL_DAYS_CODE_TABLE}
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/03/13
 * Time: 14:28
 */
public class SpecialDayTableMessageEntry implements MessageEntryCreator {

    private final String codeTableAttributeName;

    public SpecialDayTableMessageEntry(String codeTableAttributeName) {
        this.codeTableAttributeName = codeTableAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute codeTableAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeTableAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.TOU_SPECIAL_DAYS);
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE, codeTableAttribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
