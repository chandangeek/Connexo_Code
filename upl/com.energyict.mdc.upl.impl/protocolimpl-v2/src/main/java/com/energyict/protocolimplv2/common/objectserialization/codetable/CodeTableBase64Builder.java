package com.energyict.protocolimplv2.common.objectserialization.codetable;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeObject;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copyrights EnergyICT
 * Date: 29/03/11
 * Time: 10:19
 */
public class CodeTableBase64Builder {

    /**
     * @param calendar the TariffCalendar for which the XML string should be formed
     * @return
     */
    public static String getXmlStringFromCodeTable(TariffCalendar calendar, TariffCalendarExtractor calendarExtractor) {
        return new String(getBase64FromCodeTable(calendar, calendarExtractor)).replaceFirst("<[?]*(.*)[?]>", "");
    }

    /**
     * @param calendar
     * @return
     */
    public static byte[] getBase64FromCodeTable(TariffCalendar calendar, TariffCalendarExtractor calendarExtractor) {
        try {
            if (calendar == null) {
                throw new IllegalArgumentException("Code table not found: null");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out));
            oos.writeObject(CodeObject.fromCode(calendar, calendarExtractor));
            oos.flush();
            oos.close();

            return new BASE64Encoder().encode(out.toByteArray()).getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to get xml from code table: " + e.getMessage(), e);
        }
    }
}
