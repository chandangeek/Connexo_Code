package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;

import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

/**
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 13:33
 */
public class LoadProfileRegisterRequestMessageEntry implements MessageEntryCreator {

    private static final String MESSAGETAG = "LoadProfileRegister";

    private final String loadProfileAttributeName;
    private final String fromDateAttributeName;

    public LoadProfileRegisterRequestMessageEntry(String loadProfileAttributeName, String fromDateAttributeName) {
        this.loadProfileAttributeName = loadProfileAttributeName;
        this.fromDateAttributeName = fromDateAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute loadProfileAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, loadProfileAttributeName);
        OfflineDeviceMessageAttribute fromDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, fromDateAttributeName);
        final String loadProfileRegisterMessage = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
                MESSAGETAG,
                fromDateAttribute.getValue(),
                loadProfileAttribute.getValue());

        return MessageEntry
                    .fromContent(loadProfileRegisterMessage)
                    .andMessage(offlineDeviceMessage)
                    .finish();
    }
}
