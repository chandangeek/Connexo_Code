/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.io.IOException;

public class TimeOfUseMessageEntry implements MessageEntryCreator {

    public static final String SEPARATOR = "|";
    public static final String ESCAPED_SEPARATOR = "\\|";
    private final String nameAttributeName;
    private final String activationDateAttributeName;
    private final String codeIdAttributeName;

    /**
     * Default constructor
     */
    public TimeOfUseMessageEntry(String nameAttributeName, String activationDateAttributeName, String codeIdAttributeName) {
        this.nameAttributeName = nameAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
        this.codeIdAttributeName = codeIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute name = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, nameAttributeName);
        OfflineDeviceMessageAttribute activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        OfflineDeviceMessageAttribute codeTableIdAndDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeIdAttributeName);
        MessageTag messageTag = new MessageTag("TimeOfUse");
        messageTag.add(new MessageAttribute("name", name.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageAttribute("activationDate", activationDate.getDeviceMessageAttributeValue()));

        String[] split = codeTableIdAndDescription.getDeviceMessageAttributeValue().split(ESCAPED_SEPARATOR);
        String codeTableId = split[0];
        String codeTableDescription = codeTableIdAndDescription.getDeviceMessageAttributeValue().substring(codeTableId.length() + SEPARATOR.length());

        MessageTag codeIdTag = new MessageTag("CodeId");
        codeIdTag.add(new MessageValue(codeTableId));      //ID of the code table
        messageTag.add(codeIdTag);

        MessageTag activityCalendarTag = new MessageTag("Activity_Calendar");
        codeTableDescription = codeTableDescription.replace("<ActivationDate>0</ActivationDate>", "<ActivationDate>" + activationDate.getDeviceMessageAttributeValue() + "</ActivationDate>");
        codeTableDescription = codeTableDescription.replace("<CalendarName>0</CalendarName>", "<CalendarName>" + name.getDeviceMessageAttributeValue() + "</CalendarName>");
        activityCalendarTag.add(new MessageValue(encode(codeTableDescription)));
        messageTag.add(activityCalendarTag);

        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    /**
     * Base64 encode a given xml string
     */
    private String encode(String codeTableDescription) {
        try {
            return ProtocolTools.compress(codeTableDescription);
        } catch (IOException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

}