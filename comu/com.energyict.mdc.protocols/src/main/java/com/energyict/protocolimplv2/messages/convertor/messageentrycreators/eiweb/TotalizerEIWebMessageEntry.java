package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.device.data.MessageEntry;
import com.energyict.mdc.protocol.device.offline.OfflineDeviceMessage;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class TotalizerEIWebMessageEntry extends AbstractEIWebMessageEntry {


    private static final String LEGACY_TOTALIZER_TAG = "Totaliser";

    /**
     * Default constructor
     */
    public TotalizerEIWebMessageEntry() {
    }

    /**
     * <Totaliser id="11">
     * <SumMask>111</SumMask>
     * </Totaliser>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_TOTALIZER_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getDeviceMessageAttributeValue()));
        MessageTag messageValueTag = new MessageTag(getMessageName(offlineDeviceMessage));
        messageValueTag.add(new MessageValue(getValueAttribute(offlineDeviceMessage)));
        messageParentTag.add(messageValueTag);
        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}