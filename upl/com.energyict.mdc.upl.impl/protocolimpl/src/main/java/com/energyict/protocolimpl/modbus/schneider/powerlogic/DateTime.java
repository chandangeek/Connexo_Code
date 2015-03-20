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

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minutes;
    private int milliSeconds;

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
    protected Calendar getMeterCalender() {
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
    private DateTime createDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(year, (month - 1), day, hour, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, milliSeconds);
        System.out.println(cal.getTime());
        return new DateTime(cal);
    }
}