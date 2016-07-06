package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.protocol.ProtocolUtils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author sva
 * @since 18/03/2015 - 16:52
 *
 */
public class DateTime {

    private Calendar meterCalendar;

    private static int year;
    private static int month;
    private static int day;
    private static int hour;
    private static int minutes;
    private static int milliSeconds;

    private static int dayProfile = Integer.parseInt("0");
    private static int monthProfile = Integer.parseInt("0");
    private static int yearProfile = Integer.parseInt("2");
    private static int hourProfile = Integer.parseInt("1");
    private static int minuteProfile = Integer.parseInt("1");
    private static int secondsProfile = Integer.parseInt("2");

    private static int yearIndex = 0;
    private static int monthIndex = 1;
    private static int dayIndex = 2;
    private static int hourIndex = 3;
    private static int minuteIndex = 4;

    public DateTime() {
    }

    /**
     * Private constructor
     *
     * @param meterCalendar
     */
    public DateTime(Calendar meterCalendar) {
        this.meterCalendar = meterCalendar;
    }

    /**
     * Getter for the meterCalendar
     *
     * @return the metercalendar
     */
    public Calendar getMeterCalender() {
        return this.meterCalendar;
    }

    /**
     * Create a DateTime Object for a normal dateTime object.
     *
     * @param dateTimeRegisters the register read from the meter
     * @return a DateTime object with the date and time calculated form the input
     */
    public DateTime parseDateTime(int[] dateTimeRegisters) {
        if (dateTimeRegisters.length != 4) {
            throw new IllegalArgumentException("The dateTime did not contain 8 values but " + dateTimeRegisters.length);
        }

        year = 2000 + (dateTimeRegisters[0] & 0x007F);
        month = (dateTimeRegisters[1] & 0x0F00) >> 8;
        day = (dateTimeRegisters[1] & 0x001F);
        hour = (dateTimeRegisters[2] & 0x1F00) >> 8;
        minutes = dateTimeRegisters[2] & 0x003F;
        milliSeconds = (dateTimeRegisters[3] & 0xFFFF);
        return createDateTime();
    }

       /**
     * The DateTime object is created.
     * <b>GMT TimeZone is used because the meter has no knowledge of Timezones</b>
     *
     * @return the DateTimeObject
     */
    private static DateTime createDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(year, (month - 1), day, hour, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, milliSeconds);
        System.out.println(cal.getTime());
        return new DateTime(cal);
    }

    /**
     * Create a DateTime Object for a dateTime from the LoadProfile.
     * The format of the input should be : [monthDay, hourMinut, year] (the last can also be [secondsYear], but the doc is not clear about it, it's always zero)
     * ex. [2832, 3584, 0009] -> 16 nov. 2009 14:00:00
     *
     * @param dateTimeRegisters
     * @return a DateTime object with the date and time calculated form the input
     */
    public static DateTime parseProfileDateTime(byte[] dateTimeRegisters, TimeZone timeZone){
        if(dateTimeRegisters.length != 8){
            throw new IllegalArgumentException("The dateTime did not contain 3 digits but " + dateTimeRegisters.length);
        }
        day = dateTimeRegisters[dayProfile]&0x00FF;
        month = (dateTimeRegisters[monthProfile]&0x0F00)>>8;
        year = 2000+(dateTimeRegisters[yearProfile]&0x00FF);

        hour = (dateTimeRegisters[hourProfile]&0xFF00)>>8;
        minutes = dateTimeRegisters[minuteProfile]&0x00FF;
        milliSeconds = (dateTimeRegisters[secondsProfile]&0xFF00)>>8;
        return createDateTime(timeZone);
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

    public static DateTime parseDateTime(int[] values, TimeZone timeZone) {
        if (values.length != 6) {
            throw new IllegalArgumentException("The dateTime did not contain 6 digits but " + values.length);
        }

        year = values[yearIndex];
        month = values[monthIndex];
        day = values[dayIndex];
        hour = values[hourIndex];
        minutes = values[minuteIndex];
        return createDateTime(timeZone);
    }

    /**
     * The DateTime object is created.
     *
     * @return the DateTimeObject
     */
    private static DateTime createDateTime(TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(year, month - 1, day, hour, minutes);
        cal.set(Calendar.MILLISECOND, 0);
        return new DateTime(cal);
    }

    public static DateTime parseDateTime(int y, int m, int d, int h, int mi, TimeZone timeZone) {
        year = y;
        month = m;
        day = d;
        hour = h;
        minutes = mi;
        return createDateTime(timeZone);
    }
}