package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.Code;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.abstractnta.NtaProtocol;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Calendar;

/**
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 14:16
 * Author: khe
 */
public class AbstractDsmrMessaging {

    private final NtaProtocol protocol;

    public AbstractDsmrMessaging(NtaProtocol protocol) {
        this.protocol = protocol;
    }

    protected NtaProtocol getProtocol() {
        return protocol;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return getProtocol().getDlmsSession().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return getProtocol().getDlmsSession().getMeterConfig();
    }

    public Array convertEpochToDateTimeArray(String strDate) {
        Calendar cal = Calendar.getInstance(protocol.getTimeZone());
        cal.setTimeInMillis(Long.parseLong(strDate));
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = getDLMSDayOfWeek(cal);
        OctetString date = OctetString.fromByteArray(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0x00;
        OctetString time = OctetString.fromByteArray(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    protected String convertCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }
}
