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
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));

        MessageTag messageSubTag = new MessageTag(getMessageName(offlineDeviceMessage));

        MessageTag dayTag = new MessageTag(LEGACY_DAY_TAG);
        dayTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.day).getValue()));

        MessageTag monthTag = new MessageTag(LEGACY_MONTH_TAG);
        monthTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getValue()));

        MessageTag yearTag = new MessageTag(LEGACY_YEAR_TAG);
        yearTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.year).getValue()));

        MessageTag hourTag = new MessageTag(LEGACY_HOUR_TAG);
        hourTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getValue()));

        MessageTag minuteTag = new MessageTag(LEGACY_MINUTE_TAG);
        minuteTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.minute).getValue()));

        MessageTag secondTag = new MessageTag(LEGACY_SECOND_TAG);
        secondTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.second).getValue()));

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