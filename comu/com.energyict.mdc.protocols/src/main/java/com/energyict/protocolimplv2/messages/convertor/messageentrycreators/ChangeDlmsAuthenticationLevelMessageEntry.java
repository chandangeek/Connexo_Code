/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

public class ChangeDlmsAuthenticationLevelMessageEntry implements MessageEntryCreator {

    private final String authenticationLevelAttributeName;

    public ChangeDlmsAuthenticationLevelMessageEntry(String authenticationLevelAttributeName) {
        this.authenticationLevelAttributeName = authenticationLevelAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute authenticationLevelAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, authenticationLevelAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
        messageTag.add(new MessageAttribute(RtuMessageConstant.AEE_AUTHENTICATIONLEVEL, authenticationLevelAttribute.getDeviceMessageAttributeValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
