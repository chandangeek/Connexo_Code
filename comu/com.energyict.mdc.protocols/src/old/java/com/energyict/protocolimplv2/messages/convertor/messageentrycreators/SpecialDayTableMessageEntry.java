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
        messageTag.add(new MessageAttribute(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE, codeTableAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
