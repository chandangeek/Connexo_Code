package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

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
 * Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants#MBUS_CLIENT_REMOTE_COMMISSION}
 * xml tag with an additional
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_CHANNEL} attribute.
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_SHORT_ID} attribute.
 */
public class MBusClientRemoteCommissionEntry implements MessageEntryCreator {
    private final String mBusClientChannel;
    private final String mBusShortId;

    public MBusClientRemoteCommissionEntry(String mBusClientChannel, String mBusShortId) {
        this.mBusClientChannel = mBusClientChannel;
        this.mBusShortId = mBusShortId;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute mBusChannelAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, mBusClientChannel);
        OfflineDeviceMessageAttribute mBusShortId = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, this.mBusShortId);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.MBUS_CLIENT_REMOTE_COMMISSION);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_INSTALL_CHANNEL, mBusChannelAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_SHORT_ID, mBusShortId.getValue()));
        messageTag.add(new MessageValue(" "));
        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }
}
