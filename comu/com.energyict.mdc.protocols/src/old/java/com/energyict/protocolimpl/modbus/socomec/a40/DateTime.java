package com.energyict.protocolimpl.modbus.socomec.a40;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Contains time and date values for the Socomec A40 protocol
 * 
 * @author gna
 *
 */
public class DateTime {
	
	private Calendar meterCalendar;

	private static int day;
	private static int month;
	private static int year;
	private static int hour;
	private static int minutes;
	private static int seconds;
	
	private static int dayNormal = Integer.parseInt("0");
	private static int monthNormal = Integer.parseInt("1");
	private static int yearNormal = Integer.parseInt("2");
	private static int hourNormal = Integer.parseInt("3");
	private static int minuteNormal = Integer.parseInt("4");
	private static int secondsNormal = Integer.parseInt("5");
	
	private static int dayProfile = Integer.parseInt("0");
	private static int monthProfile = Integer.parseInt("0");
	private static int yearProfile = Integer.parseInt("2");
	private static int hourProfile = Integer.parseInt("1");
	private static int minuteProfile = Integer.parseInt("1");
	private static int secondsProfile = Integer.parseInt("2");
	
	/**
	 * Private constructor
	 * 
	 * @param meterCalendar
	 */
	private DateTime(Calendar meterCalendar) {
		this.meterCalendar = meterCalendar;
	}
	
	/**
	 * Getter for the meterCalendar
	 * 
	 * @return the metercalendar
	 */
	protected Calendar getMeterCalender(){
		return this.meterCalendar;
	}

	/**
	 * Create a DateTime Object for a normal dateTime object.
	 * The format of the input should be : [day, month, year, hour, minutes, seconds]
	 * ex. [16, 11, 9, 15, 52, 46] -> 16 nov. 2009 15:52:46
	 * 
	 * @param dateTimeRegisters the register read from the meter
	 * @return a DateTime object with the date and time calculated form the input
	 */
	static DateTime parseNormalDateTime(int[] dateTimeRegisters){
		if(dateTimeRegisters.length != 6){
			throw new IllegalArgumentException("The dateTime did not contain 6 digits but " + dateTimeRegisters.length);
		}
		
		day = dateTimeRegisters[dayNormal];
		month = dateTimeRegisters[monthNormal];
		year = 2000+(dateTimeRegisters[yearNormal]);
		
		hour = dateTimeRegisters[hourNormal];
		minutes = dateTimeRegisters[minuteNormal];
		seconds = dateTimeRegisters[secondsNormal];
		return createDateTime();
	}
	
	/**
	 * Create a DateTime Object for a dateTime from the LoadProfile.
	 * The format of the input should be : [monthDay, hourMinut, year] (the last can also be [secondsYear], but the doc is not clear about it, it's always zero)
	 * ex. [2832, 3584, 0009] -> 16 nov. 2009 14:00:00
	 * 
	 * @param dateTimeRegisters
	 * @return a DateTime object with the date and time calculated form the input
	 */
	static DateTime parseProfileDateTime(int[] dateTimeRegisters){
		if(dateTimeRegisters.length != 3){
			throw new IllegalArgumentException("The dateTime did not contain 3 digits but " + dateTimeRegisters.length);
		}
		day = dateTimeRegisters[dayProfile]&0x00FF;
		month = (dateTimeRegisters[monthProfile]&0x0F00)>>8;
		year = 2000+(dateTimeRegisters[yearProfile]&0x00FF);
		
		hour = (dateTimeRegisters[hourProfile]&0xFF00)>>8;
		minutes = dateTimeRegisters[minuteProfile]&0x00FF;
		seconds = (dateTimeRegisters[secondsProfile]&0xFF00)>>8;
		return createDateTime();
	}
	
	/**
	 * The DateTime object is created.
	 * <b>GMT TimeZone is used because the meter has no knowledge of Timezones</b>
	 * 
	 * @return the DateTimeObject
	 */
	private static DateTime createDateTime(){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(year, (month-1), day, hour, minutes, seconds);
		cal.set(Calendar.MILLISECOND, Integer.parseInt("0"));
		return new DateTime(cal);
	}
}
