/*
 * VDEWTimeStamp.java
 *
 * Created on 10 januari 2005, 13:59
 */

package com.energyict.protocolimpl.iec1107.vdew;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 *
 * 02/11/2009	JME Parse two character years as follows:
 * 						year 00 -> 50 = 2000 -> 2050
 * 						year 51 -> 99 = 1951 -> 2099
 */
public class VDEWTimeStamp {

	static public final int MODE_WINTERTIME=0;
	static public final int MODE_SUMMERTIME=1;
	static public final int MODE_UTCTIME=2;

	private static final int MAXYEAR = 2050;
    private static final String YEAR = "yy";
    private static final String MONTH = "MM";
    private static final String DAY = "dd";
    private String strDateFormat = "yy/MM/dd";

	int mode;
	TimeZone timeZone;
	Calendar calendar;

	public VDEWTimeStamp(TimeZone timeZone) {
		this.timeZone=timeZone;
	}

    public void setStrDateFormat(String strDateFormat) {
        this.strDateFormat = strDateFormat;
    }

    public void parse(String data) throws ProtocolException {
		parse(data.getBytes());
	}


	public void parse(String datePart, String timePart) throws IOException {
		parse(datePart.getBytes(),timePart.getBytes());
	}

	public void parse(byte[] datePart, byte[] timePart) throws ProtocolException {
		int offset=0;
		TimeZone tz=getTimeZone();
		if ((timePart.length == 4) || (timePart.length == 6)) {
			offset = 0;
		}
		if ((timePart.length == 5) || (timePart.length == 7)) {
			offset = 1;
			setMode(ProtocolUtils.bcd2nibble(timePart,0));

			if (getMode() == MODE_UTCTIME) {
				tz = TimeZone.getTimeZone("GMT");
			}  else if (getMode() == MODE_WINTERTIME) {
				if (tz.useDaylightTime()) {	// In EIServer timezone is configured with DST switching, make sure to convert to timezone without dst switching
					tz = TimeZone.getTimeZone("GMT");
					tz.setRawOffset(getTimeZone().getRawOffset());
				}
			} else {
				// In case of summer time MODE, we use the java timezone. We suppose the configurator has correctly
				// set the device timezone.
			}
		}

		calendar = ProtocolUtils.getCleanCalendar(tz);

		// absorb the season sign for the datepart
		if (datePart.length == 6) {
			offset = 0;
		}
		if (datePart.length == 7) {
			offset = 1;
		}

        int value1 = ProtocolUtils.bcd2byte(datePart, offset) & 0xFF;
        int value2 = ProtocolUtils.bcd2byte(datePart, 2 + offset) & 0xFF;
        int value3 = ProtocolUtils.bcd2byte(datePart, 4 + offset) & 0xFF;
        int[] time = new int[]{value1, value2, value3};

        int yearIndex = getDateIndex(YEAR);
        int monthIndex = getDateIndex(MONTH);
        int dayIndex = getDateIndex(DAY);

        calendar.set(Calendar.YEAR, (getYear1900_2000(time[yearIndex])));
        calendar.set(Calendar.MONTH, (time[monthIndex] - 1));
        calendar.set(Calendar.DAY_OF_MONTH, time[dayIndex]);
		calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.bcd2byte(timePart,offset));
		calendar.set(Calendar.MINUTE,ProtocolUtils.bcd2byte(timePart,2+offset));
		if ((timePart.length == 6) || (timePart.length == 7)) {
			calendar.set(Calendar.SECOND,ProtocolUtils.bcd2byte(timePart,4+offset));
		}
	}


	public void parse(byte[] data) throws ProtocolException {
		int offset=0;
		TimeZone tz=getTimeZone();

        if (data.length == 6) {
            parse(data, "000000".getBytes());
            return;
        }

        if ((data.length == 10) || (data.length == 12)) {
			offset = 0;
		}
		if ((data.length == 11) || (data.length == 13)) {
			offset = 1;
			setMode(ProtocolUtils.bcd2nibble(data,0));

			if (getMode() == MODE_UTCTIME) {
				tz = TimeZone.getTimeZone("GMT");
			}  else if (getMode() == MODE_WINTERTIME) {
				if (tz.useDaylightTime()) {	// In EIServer timezone is configured with DST switching, make sure to convert to timezone without dst switching
					tz = TimeZone.getTimeZone("GMT");
					tz.setRawOffset(getTimeZone().getRawOffset());
				}
			} else {
				// In case of summer time MODE, we use the java timezone. We suppose the configurator has correctly
				// set the device timezone.
			}
		}

        int value1 = ProtocolUtils.bcd2byte(data, offset) & 0xFF;
        int value2 = ProtocolUtils.bcd2byte(data, 2 + offset) & 0xFF;
        int value3 = ProtocolUtils.bcd2byte(data, 4 + offset) & 0xFF;
        int[] time = new int[] {value1, value2, value3};

        int yearIndex = getDateIndex(YEAR);
        int monthIndex = getDateIndex(MONTH);
        int dayIndex = getDateIndex(DAY);

        calendar = ProtocolUtils.getCleanCalendar(tz);
        calendar.set(Calendar.YEAR, (getYear1900_2000(time[yearIndex])));
        calendar.set(Calendar.MONTH, (time[monthIndex] - 1));
        calendar.set(Calendar.DAY_OF_MONTH, time[dayIndex]);
        calendar.set(Calendar.HOUR_OF_DAY, ProtocolUtils.bcd2byte(data, 6 + offset));
        calendar.set(Calendar.MINUTE, ProtocolUtils.bcd2byte(data, 8 + offset));
        if ((data.length == 12) || (data.length == 13)) {
            calendar.set(Calendar.SECOND, ProtocolUtils.bcd2byte(data, 10 + offset));
        }
    }

    private int getDateIndex(String datePart) {
        String[] parts = strDateFormat.split("/");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.equalsIgnoreCase(datePart)) {
                return i;
            }
        }
        return 0;
    }

    /**
	 * Getter for property mode.
	 * @return Value of property mode.
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Setter for property mode.
	 * @param mode New value of property mode.
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * Getter for property timeZone.
	 * @return Value of property timeZone.
	 */
	public java.util.TimeZone getTimeZone() {
		return timeZone;
	}

	static public void main(String[] args) {
		try {
			VDEWTimeStamp vts = new VDEWTimeStamp(TimeZone.getTimeZone("ECT"));
			vts.parse("20501101408");
			System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());
			vts.parse("0501101408");
			System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());

			vts.parse("2050210","02208");
			System.out.println(vts.getCalendar().getTime()+", "+vts.getMode());

		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Getter for property calendar.
	 * @return Value of property calendar.
	 */
	public java.util.Calendar getCalendar() {
		return calendar;
	}

	/**
	 * Parse two character years as follows:
	 * 		year 00 -> 50 = 2000 -> 2050
	 * 		year 51 -> 99 = 1951 -> 2099
	 *
	 * @param year
	 * @return The correct year in the range of 1951 - 2050
	 */
	private int getYear1900_2000(int year) {
		return year + ((year <= (MAXYEAR - 2000) ? 2000 : 1900));
	}


}
