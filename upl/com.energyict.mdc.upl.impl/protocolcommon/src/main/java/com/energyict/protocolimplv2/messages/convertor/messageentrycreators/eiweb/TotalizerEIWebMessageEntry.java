package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

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
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));
        MessageTag messageValueTag = new MessageTag(getMessageName(offlineDeviceMessage));
        messageValueTag.add(new MessageValue(getValueAttribute(offlineDeviceMessage)));
        messageParentTag.add(messageValueTag);
        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}