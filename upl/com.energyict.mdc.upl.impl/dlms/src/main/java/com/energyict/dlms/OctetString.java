/*
 * OctetString.java
 *
 * Created on 3 april 2003, 17:23
 */

package com.energyict.dlms;

import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * @author Koen
 */
public class OctetString implements Serializable {

    private byte[] array;
    private static final byte[] NO_DEVIATION = new byte[]{(byte) 0x80, (byte) 0x00};
    private static final int SECONDS_PER_MINUTE = 60;

    public OctetString(byte[] data) {
        this.array = data.clone();
    }

    public byte[] getArray() {
        return this.array;
    }

    @Override
    public String toString() {
        return new String(this.array);
    }

    public ObisCode toObisCode() {
        StringBuffer strBuff = new StringBuffer();
        for (int i = 0; i < this.array.length; i++) {
            if (i > 0) {
                strBuff.append(".");
            }
            strBuff.append(Integer.toString(this.array[i] & 0xFF));
        }
        return ObisCode.fromString(strBuff.toString());
    }

    public Date toUTCDate() {
        return toDate(TimeZone.getTimeZone("GMT"));
    }

    public Date toDate(TimeZone timeZone) {
        return toCalendar(timeZone).getTime();
    }

    /**
     * Uses the timezone (bytes 9 and 10) that is specified in the AXDR DateTime octetstring
     * Or the given TimeZone if the information is unspecified (0x8000) in the timestamp
     */
    public Date toDate(AXDRDateTimeDeviationType deviationType, TimeZone deviceTimeZone) {
        TimeZone tz;
        if ((array[9] != NO_DEVIATION[0]) || (array[10] != NO_DEVIATION[1])) {
            int tOffset = ProtocolUtils.getShort(array, 9);
            int deviation = tOffset / SECONDS_PER_MINUTE;
            tz = new SimpleTimeZone(deviationType.getGmtOffset(deviation) * 3600 * 1000, deviationType.getGmtNotation(deviation));
        } else {
            tz = deviceTimeZone;
        }

        return toCalendar(tz).getTime();
    }

    public Date toDate() {
        return toCalendar(TimeZone.getDefault()).getTime();
    }

    public Calendar toCalendar(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        int year = ProtocolUtils.getShort(this.array, 0) & 0x0000FFFF;
        if (year != 0xFFFF) {
            calendar.set(calendar.YEAR, year);
        }
        int month = this.array[2] & 0xFF;
        if (month != 0xFF) {
            calendar.set(calendar.MONTH, month - 1);
        }
        int date = this.array[3] & 0xFF;
        if (date != 0xFF) {
            calendar.set(calendar.DAY_OF_MONTH, date);
        }
        int hour = this.array[5] & 0xFF;
        if (hour != 0xFF) {
            calendar.set(calendar.HOUR_OF_DAY, hour);
        }
        int minute = this.array[6] & 0xFF;
        if (minute != 0xFF) {
            calendar.set(calendar.MINUTE, minute);
        }
        int seconds = this.array[7] & 0xFF;
        if (seconds != 0xFF) {
            calendar.set(calendar.SECOND, seconds);
        }
        return calendar;
    }

    public Date toDate(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        if (date != null) {
            calendar.setTime(date);
        } else {
            calendar.clear();
        }
        int year = ProtocolUtils.getShort(this.array, 0) & 0x0000FFFF;
        if (year != 0xFFFF) {
            calendar.set(calendar.YEAR, year);
        }
        int month = this.array[2] & 0xFF;
        if (month != 0xFF) {
            calendar.set(calendar.MONTH, month - 1);
        }
        int day = this.array[3] & 0xFF;
        if (day != 0xFF) {
            calendar.set(calendar.DAY_OF_MONTH, day);
        }
        int hour = this.array[5] & 0xFF;
        if (hour != 0xFF) {
            calendar.set(calendar.HOUR_OF_DAY, hour);
        }
        int minute = this.array[6] & 0xFF;
        if (minute != 0xFF) {
            calendar.set(calendar.MINUTE, minute);
        }
        int seconds = this.array[7] & 0xFF;
        if (seconds != 0xFF) {
            calendar.set(calendar.SECOND, seconds);
        }
        return calendar.getTime();
    }

} // class OctetString
