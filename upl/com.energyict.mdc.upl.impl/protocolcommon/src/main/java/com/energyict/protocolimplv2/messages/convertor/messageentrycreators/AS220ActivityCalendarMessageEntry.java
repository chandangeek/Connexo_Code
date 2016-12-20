package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagWriter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Generates XML: <TimeOfUse>(base64 encoded XML description of CodeTable)</TimeOfUse>
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:59
 */
public class AS220ActivityCalendarMessageEntry implements MessageEntryCreator {

    private final String nameAttributeName;
    private final String activationDateAttributeName;
    private final String codeIdAttributeName;

    /**
     * Default constructor
     */
    public AS220ActivityCalendarMessageEntry(String nameAttributeName, String activationDateAttributeName, String codeIdAttributeName) {
        this.nameAttributeName = nameAttributeName;
        this.activationDateAttributeName = activationDateAttributeName;
        this.codeIdAttributeName = codeIdAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String name = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, nameAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName).getValue();
        String codeTableDescription = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, codeIdAttributeName).getValue();
        String typeTag = "TimeOfUse";

        String timeInSeconds = ProtocolTools.getEpochTimeFromString(activationDate);
        long epoch = Long.valueOf(timeInSeconds);
        epoch = epoch - TimeZone.getDefault().getOffset(epoch)/1000;
        epoch = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(new Date(epoch)) ? epoch : 1;  //Replace date in past with "1"

        codeTableDescription = codeTableDescription.replace("<ActivationDate>0</ActivationDate>", "<ActivationDate>" + String.valueOf(epoch) + "</ActivationDate>");
        codeTableDescription = codeTableDescription.replace("<CalendarName>0</CalendarName>", "<CalendarName>" + name + "</CalendarName>");
        String base64encodedXML = encode(codeTableDescription);

        MessageTag mainTag = new MessageTag(typeTag);
        mainTag.add(new MessageValue(base64encodedXML));

        return new MessageEntry(SimpleTagWriter.writeTag(mainTag), offlineDeviceMessage.getTrackingId());
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