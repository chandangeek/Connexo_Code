package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.*;

/**
 * Provides the functionality to use the DLMS DateTime object (with the TAG 25)
 */
public class DateTime extends AXDRDateTime {

    public DateTime(final Date date) {
        super(date);
    }

    public DateTime(final byte[] berEncodedData, final int offset) throws IOException {
        int ptr = offset;

        if (berEncodedData[ptr] != DLMSCOSEMGlobals.TYPEDESC_DATE_TIME) {
            throw new IOException("DateTime, invalid identifier " + berEncodedData[ptr]);
        }
        ptr = ptr + 1;

        int deviation = 0;
        if ((berEncodedData[offset + 11] != NO_DEVIATION[0]) || (berEncodedData[offset + 12] != NO_DEVIATION[1])) {
            int tOffset = (short) ProtocolUtils.getInt(berEncodedData, offset + 11, 2);
            tOffset *= -1;
            deviation = tOffset / SECONDS_PER_MINUTE;
        }

        //TimeZone tz = TimeZone.getTimeZone("GMT"+(deviation<0?"":"+")+deviation);
        TimeZone tz = new SimpleTimeZone(deviation * 3600 * 1000, "GMT" + (deviation < 0 ? "" : "+") + deviation);
        dateTime = Calendar.getInstance(tz);

        int year = ProtocolUtils.getShort(berEncodedData, ptr);
        dateTime.set(Calendar.YEAR, year);
        ptr = ptr + 2;

        int month = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.MONTH, month - 1);
        ptr = ptr + 1;

        int dayOfMonth = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        ptr = ptr + 2; // one extra: skip day of week

        int hour = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        ptr = ptr + 1;

        int minute = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.MINUTE, minute);
        ptr = ptr + 1;

        int second = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.SECOND, second);

        dateTime.set(Calendar.MILLISECOND, 0);

        ptr = ptr + 1;

        ptr = ptr + 1;

        ptr = ptr + 1;    // deviation highbyte

        ptr = ptr + 1;    // deviation lowbyte

        status = ProtocolUtils.getByte2Int(berEncodedData, ptr);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        Calendar v = getValue();

        int year = v.get(Calendar.YEAR);
        int month = v.get(Calendar.MONTH);
        int dayOfMonth = v.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = v.get(Calendar.DAY_OF_WEEK);
        int hour = v.get(Calendar.HOUR_OF_DAY);
        int minute = v.get(Calendar.MINUTE);
        int second = v.get(Calendar.SECOND);
        int hs = v.get(Calendar.MILLISECOND) / MS_PER_HS;

        return
                new byte[]{
                        DLMSCOSEMGlobals.TYPEDESC_DATE_TIME,
                        (byte) ((year & INT_HIGH_MASK) >> BITS_PER_BYTE),
                        (byte) (year & INT_LOW_MASK),
                        (byte) (month + 1),
                        (byte) (dayOfMonth),
                        (byte) (dayOfWeek - 1),
                        (byte) (hour),
                        (byte) (minute),
                        (byte) (second),
                        (byte) (hs),
                        (byte) 0x80,
                        (byte) 0x00,
                        (byte) status
                };
    }
}
