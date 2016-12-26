package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

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

    protected String convertCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    protected String convertSpecialDaysCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 1, "");
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
    protected String parseSpecialDays(Code codeTable) {
        List<CodeCalendar> calendars = codeTable.getCalendars();
        Array result = new Array();
        int dayIndex = 1;
        for (CodeCalendar codeCalendar : calendars) {
            if (codeCalendar.getSeason() == 0) {
                byte[] timeStampBytes = {(byte) ((codeCalendar.getYear() == -1) ? 0xff : ((codeCalendar.getYear() >> 8) & 0xFF)), (byte) ((codeCalendar.getYear() == -1) ? 0xff : (codeCalendar.getYear()) & 0xFF),
                        (byte) ((codeCalendar.getMonth() == -1) ? 0xFF : codeCalendar.getMonth()), (byte) ((codeCalendar.getDay() == -1) ? 0xFF : codeCalendar.getDay()),
                        (byte) ((codeCalendar.getDayOfWeek() == -1) ? 0xFF : codeCalendar.getDayOfWeek())};
                OctetString timeStamp = OctetString.fromByteArray(timeStampBytes, timeStampBytes.length);
                Unsigned8 dayType = new Unsigned8(Integer.parseInt(codeCalendar.getDayType().getName()));
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

    protected String convertLookupTable(Lookup messageAttribute) {
        StringBuilder result = new StringBuilder();
        for (LookupEntry entry : messageAttribute.getEntries()) {
            if (result.length() > 0) {
                result.append(SEPARATOR);
            }
            result.append(entry.getKey());
        }
        return result.toString();
    }
}