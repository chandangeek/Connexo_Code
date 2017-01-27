/**
 * 
 */
package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.TimeZone;


/**
 * Contains the time and date values for the Countis Ci protocol
 * 
 * @author gna
 * @since 10-dec-2009
 *
 */
public class DateTime {

	
	private static int seconds;
	private static int minutes;
	private static int day;
	private static int year;
	private static int hour;
	private static int month;
	
	private static int dayIndex = 0;
	private static int monthIndex = 1;
	private static int yearIndex = 2;
	private static int hourIndex = 3;
	private static int minuteIndex = 4;
	private static int secIndex = 5;

	private Calendar meterCalendar;
	
	/**
	 * Private constructor
	 * @param cal
	 */
	private DateTime(Calendar cal) {
		this.meterCalendar = cal;
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
	 * @param values
	 * @return
	 */
	public static DateTime parseDateTime(int[] values) {
		if(values.length != 6){
			throw new IllegalArgumentException("The dateTime did not contain 6 digits but " + values.length);
		}
		
		day = values[dayIndex];
		month = values[monthIndex];
		year = values[yearIndex] + 2000;
		hour = values[hourIndex];
		minutes = values[minuteIndex];
		seconds = values[secIndex];
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
		cal.set(Calendar.MILLISECOND, Integer.valueOf(0));
		return new DateTime(cal);
	}

	/**
	 * Construct a byteArray containing the current date in the Countis dateTime format
	 * @return byteArray contain the current Date
	 */
	public static byte[] getCurrentDate() {
		Calendar gmtCal = ProtocolUtils.getCleanGMTCalendar();
		gmtCal.setTimeInMillis(System.currentTimeMillis());
		byte[] dateArray = new byte[12];
		dateArray[0] = 0;
		dateArray[1] = (byte) gmtCal.get(Calendar.DAY_OF_MONTH);
		dateArray[2] = 0;
		dateArray[3] = (byte) (gmtCal.get(Calendar.MONTH)+1);
		dateArray[4] = 0;
		dateArray[5] = (byte) (gmtCal.get(Calendar.YEAR)-2000);
		dateArray[6] = 0;
		dateArray[7] = (byte) gmtCal.get(Calendar.HOUR_OF_DAY);
		dateArray[8] = 0;
		dateArray[9] = (byte) gmtCal.get(Calendar.MINUTE);
		dateArray[10] = 0;
		dateArray[11] = (byte) gmtCal.get(Calendar.SECOND);
		return dateArray;
	}
	
}
