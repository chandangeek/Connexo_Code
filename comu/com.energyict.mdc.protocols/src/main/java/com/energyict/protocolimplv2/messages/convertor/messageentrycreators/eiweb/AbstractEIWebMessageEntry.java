/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

public abstract class AbstractEIWebMessageEntry implements MessageEntryCreator {

    private static final String EIWEB_PREFIX = "Set";
    protected static final String LEGACY_ID_TAG = "id";
    protected static final String LEGACY_PEAKSHAVER_TAG = "Peakshaver";

    /**
     * Method that can be used for messages with 2 attributes
     * One attribute is an id, the other attribute contains a value. This method looks for this value and returns it.
     */
    protected String getValueAttribute(OfflineDeviceMessage offlineDeviceMessage) {
        for (OfflineDeviceMessageAttribute attribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (!DeviceMessageConstants.id.equals(attribute.getName())) {
                return attribute.getDeviceMessageAttributeValue();
            }
        }
        return "";
    }

    /**
     * Return the value of the id attribute
     */
    protected OfflineDeviceMessageAttribute getIdAttribute(OfflineDeviceMessage offlineDeviceMessage) {
        return MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.id);
    }


    /**
     * Creates the message parent tag based on the name of the given deviceMessage spec enum.
     */
    protected String getMessageName(OfflineDeviceMessage offlineDeviceMessage) {
        String messageName = ((Enum) offlineDeviceMessage.getSpecification()).name();
        if (messageName.startsWith(EIWEB_PREFIX)) {
            messageName = messageName.substring(EIWEB_PREFIX.length());
        }
        return messageName;
    }

    protected MessageEntry createMessageEntry(MessageTag messageTag, String trackingId) {
        return new MessageEntry(SimpleTagWriter.writeTag(messageTag), trackingId);
    }
}