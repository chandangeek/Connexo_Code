package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;

/**
 * Creates a MessageEntry based on the "TimeOfUse" xml tag with 2 attributes and 2 values
 * This is the message that can be parsed by protocols that implement the TimeOfUseMessaging interface and use the TimeOfUseMessageBuilder
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
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
        messageTag.add(new MessageAttribute("name", name.getValue()));
        messageTag.add(new MessageAttribute("activationDate", activationDate.getValue()));

        String[] split = codeTableIdAndDescription.getValue().split(ESCAPED_SEPARATOR);
        String codeTableId = split[0];
        String codeTableDescription = codeTableIdAndDescription.getValue().substring(codeTableId.length() + SEPARATOR.length());

        MessageTag codeIdTag = new MessageTag("CodeId");
        codeIdTag.add(new MessageValue(codeTableId));      //ID of the code table
        messageTag.add(codeIdTag);

        MessageTag activityCalendarTag = new MessageTag("Activity_Calendar");
        codeTableDescription = codeTableDescription.replace("<ActivationDate>0</ActivationDate>", "<ActivationDate>" + activationDate.getValue() + "</ActivationDate>");
        codeTableDescription = codeTableDescription.replace("<CalendarName>0</CalendarName>", "<CalendarName>" + name.getValue() + "</CalendarName>");
        activityCalendarTag.add(new MessageValue(encode(codeTableDescription)));
        messageTag.add(activityCalendarTag);

        return MessageEntry.fromContent(messagingProtocol.writeTag(messageTag)).andMessage(offlineDeviceMessage).finish();
    }

    /**
     * Base64 encode a given xml string
     */
    private String encode(String codeTableDescription) {
        try {
            return ProtocolTools.compress(codeTableDescription);
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
    }
}