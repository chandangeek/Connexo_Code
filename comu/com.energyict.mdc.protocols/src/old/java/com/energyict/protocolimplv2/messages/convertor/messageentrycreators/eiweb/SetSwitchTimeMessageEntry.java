package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class SetSwitchTimeMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_DAY_TAG = "Day";
    private static final String LEGACY_MONTH_TAG = "Month";
    private static final String LEGACY_YEAR_TAG = "Year";
    private static final String LEGACY_HOUR_TAG = "Hour";
    private static final String LEGACY_MINUTE_TAG = "Minute";
    private static final String LEGACY_SECOND_TAG = "Second";

    /**
     * Default constructor
     */
    public SetSwitchTimeMessageEntry() {
    }


    /**
     * <Peakshaver id="1">
     * <SwitchTime>
     * <Day>1</Day>
     * <Month>1</Month>
     * <Year>sdf</Year>
     * <Hour>sdf</Hour>
     * <Minute>sdf</Minute>
     * <Second>sdf</Second>
     * </SwitchTime>
     * </Peakshaver>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_PEAKSHAVER_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getDeviceMessageAttributeValue()));

        MessageTag messageSubTag = new MessageTag(getMessageName(offlineDeviceMessage));

        MessageTag dayTag = new MessageTag(LEGACY_DAY_TAG);
        dayTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.day).getDeviceMessageAttributeValue()));

        MessageTag monthTag = new MessageTag(LEGACY_MONTH_TAG);
        monthTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getDeviceMessageAttributeValue()));

        MessageTag yearTag = new MessageTag(LEGACY_YEAR_TAG);
        yearTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.year).getDeviceMessageAttributeValue()));

        MessageTag hourTag = new MessageTag(LEGACY_HOUR_TAG);
        hourTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getDeviceMessageAttributeValue()));

        MessageTag minuteTag = new MessageTag(LEGACY_MINUTE_TAG);
        minuteTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.minute).getDeviceMessageAttributeValue()));

        MessageTag secondTag = new MessageTag(LEGACY_SECOND_TAG);
        secondTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.second).getDeviceMessageAttributeValue()));

        messageSubTag.add(dayTag);
        messageSubTag.add(monthTag);
        messageSubTag.add(yearTag);
        messageSubTag.add(hourTag);
        messageSubTag.add(minuteTag);
        messageSubTag.add(secondTag);

        messageParentTag.add(messageSubTag);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}