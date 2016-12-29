package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.cbo.ApplicationException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Copyrights EnergyICT
 * Date: 29/03/11
 * Time: 10:19
 */
public class CodeTableBase64Builder {

    public static String getXmlStringFromCodeTable(TariffCalendar calendar, Extractor extractor) {
        return new String(getBase64FromCodeTable(calendar, extractor)).replaceFirst("<[?]*(.*)[?]>", "");
    }

    public static byte[] getBase64FromCodeTable(TariffCalendar calendar, Extractor extractor) {
        try {
            if (calendar == null) {
                throw new ApplicationException("Code table not found: null");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out));
            oos.writeObject(CodeObject.fromCode(calendar, extractor));
            oos.flush();
            oos.close();

            return new BASE64Encoder().encode(out.toByteArray()).getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}