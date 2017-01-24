package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

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
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getDeviceMessageAttributeValue()));

        MessageTag messageValue = new MessageTag(getMessageName(offlineDeviceMessage));
        messageValue.add(new MessageValue(getValueAttribute(offlineDeviceMessage)));

        messageParentTag.add(messageValue);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}