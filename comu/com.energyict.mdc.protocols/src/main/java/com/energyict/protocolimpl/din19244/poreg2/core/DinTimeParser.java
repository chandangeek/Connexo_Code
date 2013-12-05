package com.energyict.protocolimpl.din19244.poreg2.core;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class to convert epochs / timestamps into byte arrays and vice versa.
 *
 * Copyrights EnergyICT
 * Date: 2-mei-2011
 * Time: 10:05:34
 */
public class DinTimeParser {

    /**
     * Makes a new Date() with the given epoch, then converts it to the EiServer timezone.
     */
    public static Date calcDate(Poreg poreg, long epoch) throws IOException {
        Calendar eventDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        eventDate.setTime(new Date(epoch * 1000));

        Calendar localTime = Calendar.getInstance(poreg.getTimeZone());
        localTime.setLenient(true);
        localTime.set(Calendar.YEAR, eventDate.get(Calendar.YEAR));
        localTime.set(Calendar.MONTH, eventDate.get(Calendar.MONTH));
        localTime.set(Calendar.DAY_OF_MONTH, eventDate.get(Calendar.DAY_OF_MONTH));
        localTime.set(Calendar.HOUR_OF_DAY, eventDate.get(Calendar.HOUR_OF_DAY));
        localTime.set(Calendar.MINUTE, eventDate.get(Calendar.MINUTE));
        localTime.set(Calendar.SECOND, eventDate.get(Calendar.SECOND));
        localTime.set(Calendar.MILLISECOND, 0);

        if (poreg.getRegisterFactory().isDST(localTime.getTime())) {
            localTime.add(Calendar.HOUR_OF_DAY, 1);
        }
        return localTime.getTime();
    }

    /**
     * Uses a list of integers to create a date. Used for parsing register 3 (clock register).
     */
    public static Date parseValues(Poreg poreg, List<ExtendedValue> values) throws IOException {
        Calendar calendar = Calendar.getInstance(poreg.getTimeZone());
        calendar.set(Calendar.YEAR, values.get(0).getValue());
        calendar.set(Calendar.MONTH, values.get(1).getValue() - 1);
        calendar.set(Calendar.DATE, values.get(2).getValue());
        calendar.set(Calendar.DAY_OF_WEEK, values.get(3).getValue() + 1);
        calendar.set(Calendar.HOUR_OF_DAY, values.get(4).getValue());
        calendar.set(Calendar.MINUTE, values.get(5).getValue());
        calendar.set(Calendar.SECOND, values.get(6).getValue());
        return calendar.getTime();
    }

    /**
     * Parses a byte[] representing a time in DIN19244 format.
     */
    public static Date parse(TimeZone timeZone, byte[] data, int offset, int length) {
        data = ProtocolTools.getSubArray(data, offset, offset + length);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.YEAR, (data[6] & 0xFF) + 2000);
        cal.set(Calendar.MONTH, (data[5] & 0x1F) - 1);
        cal.set(Calendar.DAY_OF_WEEK, 1 + (data[4] >> 5));
        cal.set(Calendar.DAY_OF_MONTH, data[4] & 0x1F);
        cal.set(Calendar.HOUR_OF_DAY, data[3] & 0x1F);
        cal.set(Calendar.MINUTE, data[2] & 0x3F);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, ProtocolTools.getUnsignedIntFromBytesLE(data, 0, 2));
        return cal.getTime();
    }

    /**
     * Converts a given date to the DIN19244 format
     */
    public static byte[] getBytes(TimeZone timeZone, Date date, boolean dst) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setLenient(true);
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, dst ? -1 : 0);     //If DST, subtract 1 hour
        byte[] result = new byte[7];
        byte[] milliBytes = ProtocolTools.getBytesFromInt(cal.get(Calendar.MILLISECOND) + cal.get(Calendar.SECOND) * 1000, 2);

        result[0] = (byte) 0;
        result[1] = milliBytes[0];
        result[2] = (byte) cal.get(Calendar.MINUTE);
        result[3] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
        result[4] = (byte) (cal.get(Calendar.DAY_OF_MONTH) | ((cal.get(Calendar.DAY_OF_WEEK) - 1) << 5));
        result[5] = (byte) (cal.get(Calendar.MONTH) + 1);
        result[6] = (byte) (cal.get(Calendar.YEAR) - 2000);
        return result;
    }

    public static byte[] getBytes(Poreg poreg, Date date) throws IOException {
        return getBytes(poreg.getTimeZone(), date, poreg.getRegisterFactory().isDST(date));
    }

    public static byte[] getBytes(Date date) {
        return getBytes(TimeZone.getDefault(), date, false);
    }
}