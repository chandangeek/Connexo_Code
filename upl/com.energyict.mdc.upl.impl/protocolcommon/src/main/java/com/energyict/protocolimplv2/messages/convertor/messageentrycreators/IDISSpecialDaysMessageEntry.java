package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import java.io.IOException;

/**
 * Generates XML: <Special_Days> <RawContent> (base64 encoded XML description of CodeTable) </RawContent></Special_Days>
 * The calendar name is "" and the activationDate is "1" in the XML description, because they are not used in the message executor of the protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class IDISSpecialDaysMessageEntry implements MessageEntryCreator {

    private final String codeIdAttributeName;

    public IDISSpecialDaysMessageEntry(String codeIdAttributeName) {
        this.codeIdAttributeName = codeIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeIdAttributeName).getDeviceMessageAttributeValue();

        MessageTag mainTag = new MessageTag("Special_Days");
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
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }
}