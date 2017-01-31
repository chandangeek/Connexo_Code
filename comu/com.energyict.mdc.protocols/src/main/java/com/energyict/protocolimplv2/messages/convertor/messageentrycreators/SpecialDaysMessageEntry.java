/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import java.io.IOException;

public class SpecialDaysMessageEntry implements MessageEntryCreator {

    private final String codeIdAttributeName;
    private final String typeAttributeName;

    public SpecialDaysMessageEntry(String codeIdAttributeName) {
        this(null, codeIdAttributeName);
    }

    public SpecialDaysMessageEntry(String typeAttributeName, String codeIdAttributeName) {
        this.typeAttributeName = typeAttributeName;
        this.codeIdAttributeName = codeIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeIdAttributeName).getDeviceMessageAttributeValue();
        String type = "Special_Days";
        if (typeAttributeName != null) {
            String prefix = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, typeAttributeName).getDeviceMessageAttributeValue();
            type = prefix + type;
        }

        MessageTag mainTag = new MessageTag(type);
        MessageTag subTag = new MessageTag("RawContent");
        subTag.add(new MessageValue(encode(codeTableDescription)));
        mainTag.add(subTag);

        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
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