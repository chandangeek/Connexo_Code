package com.energyict.protocolimpl.iec1107.instromet.dl220.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of the DL220W his Clock Object. All date/time related objects can be fetched with this object.<br>
 * <br>
 * 
 * <b>Description of the DLSChangeOver value:</b><br>
 * "0" to "2" is displayed, corresponding to: <br>
 * <li>0 = Daylight saving off <li>1 = Daylight saving automatic according to PTB stipulation. <li>2 = Daylight
 * saving changeover via adjustable times.<br>
 * In Mode "2" any times can be set which are needed to switch from summer to winter time and back again, because they,
 * for example, deviate from the PTB times. These must then be adjusted annually if required. The following details are
 * then needed: - Changeover from winter to summer time: <b>1:4A0</b> - Changeover from summer to winter time:
 * <b>1:4A8</b> The details must be given in the format: <i>"yyyy-MM-DD,hh:mm:ss"</i>.<br>
 * <br>
 * For simplicity reasons, it is strongly advised to set the value to '0' and always set the meters' clock in GMT time.
 * This way no missings or double values will be recorded in the meter.
 * 
 * @author gna
 * @since 10-feb-2010
 * 
 */
public class ClockObject extends AbstractObject {

	/** Contains all possible clock addresses */
	private static String[] startAddresses = new String[] { "0400.0", "0400.1", "0400.2", "0400.3", "0402.0", "0403.0",
			"0407.0", "040F.0" };

	/** The startAddress of the Date and Time */
	private static int startAddressDateTime = 0;
	/** The startAddress of the Seconds since 1970 (affected by daylight saving) */
	private static int startAddressSecondsSinse = 1;
	/** The startAddress of the Date (affected by daylight saving) */
	private static int startAddressDateAffectedByDST = 2;
	/** The startAddress of the Time (affected by daylight saving) */
	private static int startAddressTimeAffectedByDST = 3;
	/** The startAddress of the Minutes meter (free of daylight saving) */
	private static int startAddressMinutesFreeOfDST = 4;
	/** The startAddress of the Hours meter (free of daylight saving) */
	private static int startAddressHoursFreeOfDST = 5;
	/** The startAddress of the DayLight savings changeover */
	private static int startAddressDLSChangeOver = 6;
	/** The startAddress of the TimeZone */
	private static int startAddressTimeZone = 7;

	/** The index of the {@link #startAddresses} Array */
	private int startAddressIndex = 0;

	/** The instance of the object */
	private int instance = 1;

	/**
	 * @param link
	 */
	public ClockObject(ProtocolLink link) {
		super(link);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		return startAddresses[startAddressIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getObjectInstance() {
		return instance;
	}

	/**
	 * Return the actual dateTime of the meter
	 * 
	 * @return the dateTime
	 * 
	 * @throws IOException
	 */
	public Calendar getDateTime() throws IOException {
		startAddressIndex = startAddressDateTime;
		return parseCalendar(getValue(), link.getTimeZone());
	}

	public void getSecondsSince() throws IOException {
		startAddressIndex = startAddressSecondsSinse;
		System.out.println("Seconds since : " + getValue());
	}

	public void getDateAffectedByDST() throws IOException {
		startAddressIndex = startAddressDateAffectedByDST;
		System.out.println("Date affected by DST : " + getValue());
	}

	public void getTimeAffectedByDST() throws IOException {
		startAddressIndex = startAddressTimeAffectedByDST;
		System.out.println("Time affected by DST : " + getValue());
	}

	public void getMinutesFreeOfDST() throws IOException {
		startAddressIndex = startAddressMinutesFreeOfDST;
		System.out.println("Minutes free of DST : " + getValue());
	}

	public void getHoursFreeOfDST() throws IOException {
		startAddressIndex = startAddressHoursFreeOfDST;
		System.out.println("Hours free of DST : " + getValue());
	}

	public void getTimeZone() throws IOException {
		startAddressIndex = startAddressTimeZone;
		System.out.println("TimeZone : " + getValue());
	}

	public void getDLSChangeOver() throws IOException {
		startAddressIndex = startAddressDLSChangeOver;
		System.out.println("DLSChangeOver : " + getValue());
	}

	private static final int DATE = 0;
	private static final int TIME = 1;
	private static final int YEAR = 0;
	private static final int MONTH = 1;
	private static final int DAY = 2;
	private static final int HOUR = 0;
	private static final int MINUTE = 1;
	private static final int SECOND = 2;
	private static final String DATE_SEPARATOR = "-";
	private static final String TIME_SEPARATOR = ":";
	private static final String DATE_TIME_SEPARATOR = ",";

	/**
	 * Construct a {@link Calendar} based on the raw input data.
	 * The used timeZone is GMT
	 * 
	 * @param rawDateTime
	 *            - the raw Date and Time
	 * 
	 * @return a Calender with the metertime
	 * 
	 * @deprecated since 08/03/10, use the {@link #parseCalendar(String, TimeZone)} instead
	 */
	@Deprecated
	public static Calendar parseCalendar(String rawDateTime) {
		return parseCalendar(rawDateTime, TimeZone.getTimeZone("GMT"));
	}
	
	/**
	 * Construct a {@link Calendar} based on the raw input data and the given {@link TimeZone}
	 * 
	 * @param rawDateTime
	 *          - the raw Date and Time
	 * @param timeZone
	 * 			- the timeZone the rawDateTime is written in
	 * 
	 * @return a Calender with the metertime
	 */
	public static Calendar parseCalendar(String rawDateTime, TimeZone timeZone){
		Calendar meterCal = Calendar.getInstance(timeZone);
		String[] dateTime = rawDateTime.split(DATE_TIME_SEPARATOR);
		String[] date = dateTime[DATE].split(DATE_SEPARATOR);
		String[] time = dateTime[TIME].split(TIME_SEPARATOR);

		meterCal.set(Calendar.YEAR, Integer.valueOf(date[YEAR]));
		meterCal.set(Calendar.MONTH, Integer.valueOf(date[MONTH]) - 1); // Java calendar starts at month 1
		meterCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date[DAY]));

		meterCal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[HOUR]));
		meterCal.set(Calendar.MINUTE, Integer.valueOf(time[MINUTE]));
		meterCal.set(Calendar.SECOND, Integer.valueOf(time[SECOND]));
		meterCal.set(Calendar.MILLISECOND, 0);

		return meterCal;
	}

	/**
	 * Construct a string in the <i>yyyy-MM-DD,HH:mm:ss</i> format
	 * 
	 * @param newCalendar
	 *            - the {@link Calendar} to convert
	 * 
	 * @return the new calendar string for the meter
	 */
	protected static String getRawData(Calendar newCalendar) {
        return convertToTwoDigits(newCalendar.get(Calendar.YEAR)) + DATE_SEPARATOR +
               convertToTwoDigits(newCalendar.get(Calendar.MONTH) + 1) + DATE_SEPARATOR +
               convertToTwoDigits(newCalendar.get(Calendar.DAY_OF_MONTH)) + DATE_TIME_SEPARATOR +
               convertToTwoDigits(newCalendar.get(Calendar.HOUR_OF_DAY)) + TIME_SEPARATOR +
               convertToTwoDigits(newCalendar.get(Calendar.MINUTE)) + TIME_SEPARATOR +
               convertToTwoDigits(newCalendar.get(Calendar.SECOND));
	}

	/**
	 * Return a two digit string value
	 * 
	 * @param possibleOneDigitValue
	 *            - a string value with possibly 1 digit
	 * @return a two digit string value
	 */
	private static String convertToTwoDigits(int possibleOneDigitValue) {
		if (Integer.toString(possibleOneDigitValue).length() == 1) {
			return "0" + possibleOneDigitValue;
		} else {
			return Integer.toString(possibleOneDigitValue);
		}
	}

	/**
	 * Write the current time to the meter
	 * 
	 * @throws IOException when the write failed
	 */
	public void writeClock() throws IOException {
		Calendar currentCalendar = Calendar.getInstance(link.getTimeZone());
		setValue(getRawData(currentCalendar).getBytes());
	}
}
