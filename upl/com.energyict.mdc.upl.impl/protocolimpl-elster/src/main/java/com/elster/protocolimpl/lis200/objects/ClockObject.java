package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of the DL220W his Clock Object. All date/time related objects
 * can be fetched with this object.<br>
 * <br>
 * 
 * <b>Description of the DLSChangeOver value:</b><br>
 * "0" to "2" is displayed, corresponding to: <br>
 * <li>0 = Daylight saving off <li>1 = Daylight saving automatic according
 * to PTB stipulation. <li>2 = Daylight saving changeover via adjustable
 * times.<br>
 * In Mode "2" any times can be set which are needed to switch from summer to
 * winter time and back again, because they, for example, deviate from the PTB
 * times. These must then be adjusted annually if required. The following
 * details are then needed: - Changeover from winter to summer time:
 * <b>1:4A0</b> - Changeover from summer to winter time: <b>1:4A8</b> The
 * details must be given in the format: <i>"yyyy-MM-DD,hh:mm:ss"</i>.<br>
 * <br>
 * For simplicity reasons, it is strongly advised to set the value to '0' and
 * always set the meters' clock in GMT time. This way no missing or double
 * values will be recorded in the meter.
 * 
 * @author gna
 * @since 10-feb-2010
 * 
 *        modified gh, 12-apr-2010
 */
public class ClockObject extends AbstractObject {

	/** SimpleDateFormat class to convert LIS200 date to normal date */
	/* (defined as static to speed up conversion) */
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd,HH:mm:ss");

	/** Contains all possible clock addresses */
	private static String[] startAddresses = new String[] { "0400.0" };

	/** The startAddress of the Date and Time */
	private static int startAddressDateTime = 0;

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
		String rawDate = getValue();
		link.getLogger().info("Date read from device (raw data):" + rawDate);
		Calendar c = parseCalendar(rawDate, false, link.getTimeZone());
		link.getLogger().info("Date read from device (calendar):" + c);
		
		Calendar currentCalendar = Calendar.getInstance(link.getTimeZone());
        link.getLogger().info("Current Date (raw format)       :" + getRawData(currentCalendar));
		return c;
	}

	/**
	 * Write the current time to the meter
	 * 
	 * @throws IOException
	 *             when the write failed
	 */
	public void writeClock() throws IOException {
		Calendar currentCalendar = Calendar.getInstance(link.getTimeZone());
		setValue(getRawData(currentCalendar).getBytes());
	}

	/**
	 * Construct a {@link Calendar} based on the raw input data. The used
	 * timeZone is GMT
	 * 
	 * @param rawDateTime
	 *            - the raw Date and Time
	 * 
	 * @return a calendar with the meter time
	 * 
	 * @deprecated since 08/03/10, use the
	 *             parseCalendar(String, TimeZone) instead
	 */
	@Deprecated
	public static Calendar parseCalendar(String rawDateTime) {
		return parseCalendar(rawDateTime, false, TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Construct a {@link Calendar} based on the raw input data and the given
	 * {@link TimeZone}
	 * 
	 * @param rawDateTime
	 *            - the raw Date and Time
	 * @param isSummerTime
	 *            - flag to define if given rawDateTime is summer time
	 * @param timeZone
	 *            - the timeZone the rawDateTime is written in
	 * 
	 * @return a calendar with the meter time
	 */
	public static Calendar parseCalendar(String rawDateTime,
			boolean isSummerTime, TimeZone timeZone) {

		Calendar meterCal = Calendar.getInstance(timeZone);

		try {
            sdf.setTimeZone(timeZone);
			meterCal.setTime(sdf.parse(rawDateTime));

			/*
			 * this piece of code corrects time when switching from summer to
			 * winter time, and given time stamp is in first of double hour,
			 * or time zone has not DST and device is working with DST
			 */
			if ((meterCal.get(Calendar.DST_OFFSET) == 0) && isSummerTime) {
				meterCal.add(Calendar.HOUR_OF_DAY, -1);
			}
		} catch (ParseException e) {
			meterCal = null;
		}

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
	public static String getRawData(Calendar newCalendar) {
		
        sdf.setTimeZone(newCalendar.getTimeZone());
		return sdf.format(newCalendar.getTime());
		
	}
}
