package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class AnalogOutMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_ANALOGOUT_TAG = "AnalOut";
    private static final String LEGACY_VALUE_TAG = "value";

    /**
     * Default constructor
     */
    public AnalogOutMessageEntry() {
    }

    /**
     * <AnalOut id="17">
     * <value>1</value>
     * </AnalOut>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_ANALOGOUT_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));

        MessageTag messageValue = new MessageTag(LEGACY_VALUE_TAG);
        messageValue.add(new MessageValue(getValueAttribute(offlineDeviceMessage)));

        messageParentTag.add(messageValue);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}