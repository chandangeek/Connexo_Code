package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author sva
 * @since 18/03/2015 - 16:52
 *
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
     * Create a DateTime Object for the given byte array, which corresponds to a file record
     * of the profile data logs
     * @param bytes the file record
     * @param offset the offset within the record
     * @return a DateTime object with the date and time calculated from the input
     */
    public DateTime parseProfileEntryDateTime(byte[] bytes, int offset) {
        year = 2000 + ProtocolTools.getIntFromBytes(bytes, offset, 2);
        offset += 2;
        month = (bytes[offset++] & 0xFF);
        day = (bytes[offset++] & 0xFF);
        hour = (bytes[offset++] & 0xFF);
        minutes = (bytes[offset++] & 0xFF);
        milliSeconds = ProtocolTools.getIntFromBytes(bytes, offset, 2);
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
        return new DateTime(cal);
    }
}