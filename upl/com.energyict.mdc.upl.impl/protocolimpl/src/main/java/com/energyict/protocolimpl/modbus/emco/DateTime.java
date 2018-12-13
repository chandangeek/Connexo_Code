package com.energyict.protocolimpl.modbus.emco;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.TimeZone;


/**
 * Contains the time and date values for the FP93B protocol
 */
public class DateTime {

    private static int seconds;
    private static int minutes;
    private static int day;
    private static int year;
    private static int hour;
    private static int month;

    private static int yearIndex = 0;
    private static int monthIndex = 1;
    private static int dayIndex = 2;
    private static int hourIndex = 3;
    private static int minuteIndex = 4;
    private static int secIndex = 5;

    private Calendar meterCalendar;

    /**
     * Private constructor
     */
    private DateTime(Calendar cal) {
        this.meterCalendar = cal;
    }

    /**
     * Getter for the meterCalendar
     *
     * @return the meter calendar
     */
    public Calendar getMeterCalender() {
        return this.meterCalendar;
    }

    public static DateTime parseDateTime(int[] values, TimeZone timeZone) {
        if (values.length != 6) {
            throw new IllegalArgumentException("The dateTime did not contain 6 digits but " + values.length);
        }

        year = values[yearIndex];
        month = values[monthIndex];
        day = values[dayIndex];
        hour = values[hourIndex];
        minutes = values[minuteIndex];
        seconds = values[secIndex];
        return createDateTime(timeZone);
    }

    /**
     * The DateTime object is created.
     *
     * @return the DateTimeObject
     */
    private static DateTime createDateTime(TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(year, month - 1, day, hour, minutes, seconds);
        cal.set(Calendar.MILLISECOND, 0);
        return new DateTime(cal);
    }

    /**
     * Construct a byteArray containing the current date in the FP93B dateTime format
     *
     * @return byteArray contain the current Date
     */
    public static byte[] getCurrentDate(TimeZone timeZone) {
        Calendar gmtCal = ProtocolUtils.getCalendar(timeZone);
        gmtCal.setTimeInMillis(System.currentTimeMillis());
        byte[] dateArray = new byte[6];
        dateArray[0] = (byte) gmtCal.get(Calendar.YEAR);
        dateArray[1] = (byte) (gmtCal.get(Calendar.MONTH) + 1);
        dateArray[2] = (byte) (gmtCal.get(Calendar.DAY_OF_MONTH));
        dateArray[3] = (byte) gmtCal.get(Calendar.HOUR_OF_DAY);
        dateArray[4] = (byte) gmtCal.get(Calendar.MINUTE);
        dateArray[5] = (byte) gmtCal.get(Calendar.SECOND);
        return dateArray;
    }
}
