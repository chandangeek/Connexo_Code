package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class SetSetpointMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_CURRENTVALUE_TAG = "CurrentValue";
    private static final String LEGACY_NEWVALUE_TAG = "NewValue";

    /**
     * Default constructor
     */
    public SetSetpointMessageEntry() {
    }


    /**
     * <Peakshaver id="1">
     * <Setpoint tariff="1">
     * <CurrentValue>1</CurrentValue>
     * <NewValue>1</NewValue>
     * </Setpoint>
     * </Peakshaver>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_PEAKSHAVER_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));

        MessageTag messageSubTag = new MessageTag(getMessageName(offlineDeviceMessage));
        String tariffAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.tariff).getValue();
        messageSubTag.add(new MessageAttribute(DeviceMessageConstants.tariff, tariffAttribute));

        MessageTag currentValueTag = new MessageTag(LEGACY_CURRENTVALUE_TAG);
        currentValueTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.CurrentValueAttributeName).getValue()));

        MessageTag newValueTag = new MessageTag(LEGACY_NEWVALUE_TAG);
        newValueTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.NewValueAttributeName).getValue()));

        messageSubTag.add(currentValueTag);
        messageSubTag.add(newValueTag);

        messageParentTag.add(messageSubTag);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}