package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author sva
 * @since 18/03/2015 - 16:52
 *
 * @Deprecated - currently out of scope of the protocol
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
    public static DateTime parseProfileDateTime(byte[] dateTimeRegisters){
        if(dateTimeRegisters.length != 3){
            throw new IllegalArgumentException("The dateTime did not contain 3 digits but " + dateTimeRegisters.length);
        }
        day = dateTimeRegisters[dayProfile]&0x00FF;
        month = (dateTimeRegisters[monthProfile]&0x0F00)>>8;
        year = 2000+(dateTimeRegisters[yearProfile]&0x00FF);

        hour = (dateTimeRegisters[hourProfile]&0xFF00)>>8;
        minutes = dateTimeRegisters[minuteProfile]&0x00FF;
        milliSeconds = (dateTimeRegisters[secondsProfile]&0xFF00)>>8;
        return createDateTime();
    }
}