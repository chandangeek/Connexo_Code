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
public class EIWebConfigurationMessageEntry extends AbstractEIWebMessageEntry {

    public static final String LEGACY_EIWEB_TAG = "EIWeb";

    /**
     * Default constructor
     */
    public EIWebConfigurationMessageEntry() {
    }

    /**
     * <EIWeb id="1">
     * <EIWebCurrentInterval>dfdf</EIWebCurrentInterval>
     * </EIWeb>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_EIWEB_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));

        MessageTag messageValue = new MessageTag(getMessageName(offlineDeviceMessage));
        messageValue.add(new MessageValue(getValueAttribute(offlineDeviceMessage)));

        messageParentTag.add(messageValue);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}