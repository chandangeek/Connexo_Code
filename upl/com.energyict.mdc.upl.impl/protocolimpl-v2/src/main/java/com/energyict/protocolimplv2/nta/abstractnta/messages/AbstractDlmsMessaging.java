package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 14:16
 * Author: khe
 */
public class AbstractDlmsMessaging {

    private final AbstractDlmsProtocol protocol;

    public static final String SEPARATOR = ";";

    public AbstractDlmsMessaging(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    public AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    protected String convertCodeTableToXML(com.energyict.mdc.upl.properties.TariffCalendar calendar, TariffCalendarExtractor extractor) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(calendar, extractor, 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    protected String convertSpecialDaysCodeTableToXML(com.energyict.mdc.upl.properties.TariffCalendar messageAttribute, TariffCalendarExtractor extractor) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, extractor, 1, "");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    protected String convertCodeTableToAXDR(ActivityCalendarMessage parser) {
        try {
            parser.parse();
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
        String dayProfile = ProtocolTools.getHexStringFromBytes(parser.getDayProfile().getBEREncodedByteArray(), "");
        String weekProfile = ProtocolTools.getHexStringFromBytes(parser.getWeekProfile().getBEREncodedByteArray(), "");
        String seasonProfile = ProtocolTools.getHexStringFromBytes(parser.getSeasonProfile().getBEREncodedByteArray(), "");
        return dayProfile + "|" + weekProfile + "|" + seasonProfile;
    }

    /**
     * Parse the special days of the given code table into the proper AXDR array.
     */
    protected String parseSpecialDays(TariffCalendar calendar, TariffCalendarExtractor extractor) {
        Array result = new Array();
        int dayIndex = 1;
        for (TariffCalendarExtractor.CalendarRule rule : extractor.rules(calendar)) {
            if (!rule.seasonId().isPresent()) {
                byte[] timeStampBytes = {(byte) ((rule.year() == -1) ? 0xff : ((rule.year() >> 8) & 0xFF)), (byte) ((rule.year() == -1) ? 0xff : (rule.year()) & 0xFF),
                        (byte) ((rule.month() == -1) ? 0xFF : rule.month()), (byte) ((rule.day() == -1) ? 0xFF : rule.day()),
                        (byte) ((rule.dayOfWeek() == -1) ? 0xFF : rule.dayOfWeek())};
                OctetString timeStamp = OctetString.fromByteArray(timeStampBytes, timeStampBytes.length);
                Unsigned8 dayType = new Unsigned8(Integer.parseInt(rule.dayTypeName()));
                Structure specialDayStructure = new Structure();
                specialDayStructure.addDataType(new Unsigned16(dayIndex));
                specialDayStructure.addDataType(timeStamp);
                specialDayStructure.addDataType(dayType);
                result.addDataType(specialDayStructure);
                dayIndex++;
            }
        }
        return ProtocolTools.getHexStringFromBytes(result.getBEREncodedByteArray(), "");
    }

    protected String convertLookupTable(NumberLookup messageAttribute, NumberLookupExtractor extractor) {
        return extractor.keys(messageAttribute).stream().collect(Collectors.joining(SEPARATOR));
    }

}