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
import java.util.*;

/**
 * Generates XML: <Activity_Calendar> <RawContent> (base64 encoded XML description of CodeTable) </RawContent></Activity_Calendar>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class ActivityCalendarMessageEntry implements MessageEntryCreator {

    private final String nameAttributeName;
    private final String activationDateAttributeName;
    private final String codeIdAttributeName;
    private final String typeAttributeName;

    /**
     * Default constructor
     */
    public ActivityCalendarMessageEntry(String nameAttributeName, String activationDateAttributeName, String codeIdAttributeName) {
        this(null, nameAttributeName, activationDateAttributeName, codeIdAttributeName);    //Use the default main tag: "Activity_Calendar"
    }

    public ActivityCalendarMessageEntry(String typeAttributeName, String nameAttributeName, String activationDateAttributeName, String codeIdAttributeName) {
        this.typeAttributeName = typeAttributeName;
        this.nameAttributeName = nameAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
        this.codeIdAttributeName = codeIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String name = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, nameAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName).getDeviceMessageAttributeValue();
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeIdAttributeName).getDeviceMessageAttributeValue();
        String typeTag = "Activity_Calendar";
        if (typeAttributeName != null) {
            String prefix = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, typeAttributeName).getDeviceMessageAttributeValue();
            typeTag = prefix + typeTag;
        }

        long epoch = Long.valueOf(activationDate);
        epoch = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(new Date(epoch)) ? epoch : 1;  //Replace date in past with "1"

        codeTableDescription = codeTableDescription.replace("<ActivationDate>0</ActivationDate>", "<ActivationDate>" + String.valueOf(epoch) + "</ActivationDate>");
        codeTableDescription = codeTableDescription.replace("<CalendarName>0</CalendarName>", "<CalendarName>" + name + "</CalendarName>");
        String base64encodedXML = encode(codeTableDescription);

        MessageTag mainTag = new MessageTag(typeTag);
        MessageTag subTag = new MessageTag("RawContent");
        subTag.add(new MessageValue(base64encodedXML));
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