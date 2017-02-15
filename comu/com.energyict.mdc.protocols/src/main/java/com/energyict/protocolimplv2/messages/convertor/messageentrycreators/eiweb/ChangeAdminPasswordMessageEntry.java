/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

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

        String adminOldValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.AdminOld).getDeviceMessageAttributeValue();
        String adminNewValue = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.AdminNew).getDeviceMessageAttributeValue();

        MessageTag oldTag = new MessageTag(LEGACY_ADMINOLD_TAG);
        oldTag.add(new MessageValue(adminOldValue));

        MessageTag newTag = new MessageTag(LEGACY_ADMINNEW_TAG);
        newTag.add(new MessageValue(adminNewValue));

        return new MessageEntry(SimpleTagWriter.writeTag(oldTag) + SimpleTagWriter.writeTag(newTag), offlineDeviceMessage.getTrackingId());
    }
}