package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.Messaging;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.messaging.LegacyMessageConverter;

import javax.xml.parsers.ParserConfigurationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 15:05
 */
public abstract class AbstractMessageConverter implements LegacyMessageConverter {

    protected static final String EMPTY_FORMAT = "";
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    protected final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected final SimpleDateFormat dateTimeFormatWithTimeZone = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    protected final SimpleDateFormat europeanDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private Messaging messagingProtocol;

    /**
     * Get the registry which contains the mapping between the DeviceMessageSpecs
     * and the MessageEntryCreators.
     *
     * @return the registry mapping
     */
    protected abstract Map<DeviceMessageSpec, MessageEntryCreator> getRegistry();

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>(getRegistry().keySet());
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        final DeviceMessageSpec deviceMessageSpec = MessageConverterTools.getDeviceMessageSpecForOfflineDeviceMessage(offlineDeviceMessage);

        final MessageEntryCreator messageEntryCreator = getRegistry().get(deviceMessageSpec);
        if (messageEntryCreator != null) {
            return messageEntryCreator.createMessageEntry(getMessagingProtocol(), offlineDeviceMessage);
        } else {
            return new MessageEntry("", "");
        }
    }

    @Override
    public void setMessagingProtocol(Messaging messaging) {
        this.messagingProtocol = messaging;
    }

    protected Messaging getMessagingProtocol() {
        return this.messagingProtocol;
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     * It is up to the message entry creator to replace them with the values of the attributes
     */
    protected String convertCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

    protected String convertSpecialDaysCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 1, "");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

}