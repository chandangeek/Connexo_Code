package com.elster.protocolimpl.dlms.tariff;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.elster.protocolimpl.dlms.tariff.objects.CodeObject;
import com.energyict.cbo.ApplicationException;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;

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
     * @param codeTableId
     * @return
     */
    public static String getXmlStringFromCodeTable(int codeTableId, TariffCalendarFinder finder, TariffCalendarExtractor extractor) {
        return new String(getBase64FromCodeTable(finder.from(Integer.toString(codeTableId)).orElse(null), extractor)).replaceFirst("<[?]*(.*)[?]>", "");
    }


    public static String getXmlStringFromCodeTable(TariffCalendar calendar, TariffCalendarExtractor extractor) {
        return new String(getBase64FromCodeTable(calendar, extractor)).replaceFirst("<[?]*(.*)[?]>", "");
    }

    /**
     * @param codeTableId
     * @return
     */
    public static byte[] getBase64FromCodeTable(int codeTableId, TariffCalendarFinder finder, TariffCalendarExtractor extractor) {
        return getBase64FromCodeTable(finder.from(Integer.toString(codeTableId)).orElse(null), extractor);
    }

    /**
     * @param calendar
     * @return
     */
    public static byte[] getBase64FromCodeTable(TariffCalendar calendar, TariffCalendarExtractor extractor) {
        try {
            if (calendar == null) {
                throw new ApplicationException("Code table not found: null");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out));
            oos.writeObject(CodeObject.fromCode(calendar, extractor));
            oos.flush();
            oos.close();

            return new Base64EncoderDecoder().encode(out.toByteArray()).getBytes();
        } catch (Exception e) {
            throw new ApplicationException("Unable to get xml from code table: " + e.getMessage(), e);
        }
    }

    private static MeteringWarehouse mw() {
        MeteringWarehouse mw = MeteringWarehouse.getCurrent();
        if (mw == null) {
            MeteringWarehouse.createBatchContext();
            mw = MeteringWarehouse.getCurrent();
        }
        return mw;
    }

}
