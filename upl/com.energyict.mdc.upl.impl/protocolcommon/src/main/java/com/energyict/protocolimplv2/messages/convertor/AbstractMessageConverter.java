package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;

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

    private final Messaging messagingProtocol;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    protected AbstractMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.messagingProtocol = messagingProtocol;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    /**
     * Get the registry which contains the mapping between the DeviceMessageSpecs
     * and the MessageEntryCreators.
     *
     * @return the registry mapping
     */
    protected abstract Map<DeviceMessageSpec, MessageEntryCreator> getRegistry();

    protected DeviceMessageSpec messageSpec(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>(getRegistry().keySet());
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        final DeviceMessageSpec deviceMessageSpec = offlineDeviceMessage.getSpecification();
        final MessageEntryCreator messageEntryCreator = getRegistry().get(deviceMessageSpec);
        if (messageEntryCreator != null) {
            return messageEntryCreator.createMessageEntry(this.messagingProtocol, offlineDeviceMessage);
        } else {
            return MessageEntry.empty();
        }
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     * It is up to the message entry creator to replace them with the values of the attributes
     */
    protected String convertCodeTableToXML(TariffCalendar messageAttribute, TariffCalendarExtractor extractor) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, extractor, 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    protected String convertSpecialDaysCodeTableToXML(TariffCalendar messageAttribute, TariffCalendarExtractor extractor) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, extractor, 1, "");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }
}
