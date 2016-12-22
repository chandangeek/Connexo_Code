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
public class SetLoadMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_MAXOFF_TAG = "MaxOff";
    private static final String LEGACY_DELAY_TAG = "Delay";
    private static final String LEGACY_MANUAL_TAG = "Manual";
    private static final String LEGACY_STATUS_TAG = "Status";
    private static final String LEGACY_IPADDRESS_TAG = "IPAddress";
    private static final String LEGACY_CHNNBR_TAG = "ChnNbr";

    /**
     * Default constructor
     */
    public SetLoadMessageEntry() {
    }


    /**
     * <Peakshaver id="1">
     * <Load id="1">
     * <MaxOff>1</MaxOff>
     * <Delay>1</Delay>
     * <Manual>1</Manual>
     * <Status>1</Status>
     * <IPAddress>1</IPAddress>
     * <ChnNbr>1</ChnNbr>
     * </Load>
     * </Peakshaver>
     */
    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageParentTag = new MessageTag(LEGACY_PEAKSHAVER_TAG);
        messageParentTag.add(new MessageAttribute(LEGACY_ID_TAG, getIdAttribute(offlineDeviceMessage).getValue()));

        MessageTag messageSubTag = new MessageTag(getMessageName(offlineDeviceMessage));
        String loadId = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.loadIdAttributeName).getValue();
        messageSubTag.add(new MessageAttribute(LEGACY_ID_TAG, loadId));

        MessageTag maxOffTag = new MessageTag(LEGACY_MAXOFF_TAG);
        maxOffTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.MaxOffAttributeName).getValue()));

        MessageTag delayTag = new MessageTag(LEGACY_DELAY_TAG);
        delayTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.DelayAttributeName).getValue()));

        MessageTag manualTag = new MessageTag(LEGACY_MANUAL_TAG);
        manualTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.ManualAttributeName).getValue()));

        MessageTag statusTag = new MessageTag(LEGACY_STATUS_TAG);
        statusTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.StatusAttributeName).getValue()));

        MessageTag ipAddressTag = new MessageTag(LEGACY_IPADDRESS_TAG);
        ipAddressTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.PeakShaverIPAddressAttributeName).getValue()));

        MessageTag chnNbrTag = new MessageTag(LEGACY_CHNNBR_TAG);
        chnNbrTag.add(new MessageValue(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.PeakShaveChnNbrAttributeName).getValue()));

        messageSubTag.add(maxOffTag);
        messageSubTag.add(delayTag);
        messageSubTag.add(manualTag);
        messageSubTag.add(statusTag);
        messageSubTag.add(ipAddressTag);
        messageSubTag.add(chnNbrTag);

        messageParentTag.add(messageSubTag);

        return createMessageEntry(messageParentTag, offlineDeviceMessage.getTrackingId());
    }
}