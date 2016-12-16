package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class ChangeAdminPasswordMessageEntry extends AbstractEIWebMessageEntry {

    private static final String LEGACY_ADMINOLD_TAG = "AdminOld";
    private static final String LEGACY_ADMINNEW_TAG = "AdminNew";

    /**
     * Default constructor
     */
    public ChangeAdminPasswordMessageEntry() {
    }

    @Override             //E.G. <AdminOld>sdfsdf</AdminOld><AdminNew>sdfsdf2</AdminNew>
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {

        String adminOldValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.AdminOld).getValue();
        String adminNewValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.AdminNew).getValue();

        MessageTag oldTag = new MessageTag(LEGACY_ADMINOLD_TAG);
        oldTag.add(new MessageValue(adminOldValue));

        MessageTag newTag = new MessageTag(LEGACY_ADMINNEW_TAG);
        newTag.add(new MessageValue(adminNewValue));

        return new MessageEntry(SimpleTagWriter.writeTag(oldTag) + SimpleTagWriter.writeTag(newTag), offlineDeviceMessage.getTrackingId());
    }
}