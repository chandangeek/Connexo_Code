package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;

/**
 * Year offset explaned by example:
 * 
 * <pre>
 * 
 *    	TODAY YEAR : 2003
 * 
 *    	meteryear = [2000] offset=[0] todayYear=2003
 *    	meteryear = [2001] offset=[1] todayYear=2003
 *    	meteryear = [2002] offset=[2] todayYear=2003
 *    	meteryear = [2003] offset=[3] todayYear=2003
 * 
 *    	TODAY YEAR : 2004
 * 
 *    	meteryear = [2004] offset=[0] todayYear=2004
 *    	meteryear = [2001] offset=[1] todayYear=2004
 *    	meteryear = [2002] offset=[2] todayYear=2004
 *    	meteryear = [2003] offset=[3] todayYear=2004
 * 
 *    	TODAY YEAR : 2005
 * 
 *    	meteryear = [2004] offset=[0] todayYear=2005
 *    	meteryear = [2005] offset=[1] todayYear=2005
 *    	meteryear = [2002] offset=[2] todayYear=2005
 *    	meteryear = [2003] offset=[3] todayYear=2005
 * 
 *     	TODAY YEAR : 2006
 * 
 *    	meteryear = [2004] offset=[0] todayYear=2006
 *    	meteryear = [2005] offset=[1] todayYear=2006
 *    	meteryear = [2006] offset=[2] todayYear=2006
 *    	meteryear = [2003] offset=[3] todayYear=2006
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * @author fbo
 */

public class PPMUtils {

	public static final int MILLISECONDS_IN_HOUR = 60 * 60 * 1000;

	public static Long parseLongHexLE(byte[] data, int offset, int length)
	throws IOException, NumberFormatException {
		return new Long(ProtocolUtils.getLongLE(data, offset, length));
	}

	public static Long parseLongHex(byte[] data, int offset, int length)
	throws IOException, NumberFormatException {
		return new Long(ProtocolUtils.getLong(data, offset, length));
	}

	public static String buildHexLE(Long val) {
		long lVal = val.longValue();
		byte[] data = new byte[4];
		ProtocolUtils.val2HEXascii((int) lVal & 0xFF, data, 0);
		ProtocolUtils.val2HEXascii((int) (lVal >> 8) & 0xFF, data, 2);

		return new String(data);
	}

	public static BigDecimal parseBigDecimal(byte[] data, int offset,
			int length, Unit unit) throws IOException, NumberFormatException {
		if (length > 8) {
			throw new IOException(
			"Register, parseBigDecimal, datalength should not exceed 8!");
		}
		BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long
				.toHexString(ProtocolUtils.getLongLE(data, offset, length))));
		return bd.movePointLeft(Math.abs(unit.getScale()));
	}

	public static Quantity parseQuantity(byte[] data, int offset, int length,
			BigDecimal scale, Unit unit) throws IOException,
			NumberFormatException {

		if (length > 8) {
			throw new IOException(
			"Register, parseQuantity, datalength should not exceed 8!");
		}
		BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long
				.toHexString(ProtocolUtils.getLong(data, offset, length))));
		if (scale == null) {
			bd = new BigDecimal(0);
		} else {
			bd = bd.multiply(scale);
		}
		return new Quantity(bd, unit);
	}

	public static Long parseBitfield(byte[] data, int offset, int length)
	throws IOException {
		if (length > 8) {
			throw new IOException(
			"Register, parseBitfield, datalength should not exceed 8!");
		}
		return new Long(ProtocolUtils.getLong(data, offset, length));
	}

	public static Long parseLong(byte[] data, int offset, int length)
	throws IOException, NumberFormatException {
		if (length > 8) {
			throw new IOException(
			"Register, parseLong, datalength should not exceed 8!");
		}
		return new Long(Long.parseLong(Long.toHexString(ProtocolUtils
				.getLongLE(data, offset, length))));
	}

	public static Integer parseInteger(byte[] data, int offset, int length)
	throws IOException, NumberFormatException {
		if (length > 4) {
			throw new IOException(
			"Register, parseInteger, datalength should not exceed 4!");
		}

		Integer result = new Integer(Integer.parseInt(Integer
				.toHexString(ProtocolUtils.getIntLE(data, offset, length))));

		return result;
	}

	// Parse a BCD String from native meter format
	public static String parseBCDString(byte[] data) {
		StringBuffer result = new StringBuffer();
		if (data == null) {
			result.append("null");
		} else {
			for (int i = 0; i < data.length; i++) {
				result.append((char) (data[i] & 0xFF));
			}
		}
		return result.toString();
	}

	/** Parse a Date (7 byte full date) */
	public static Date parseDate(byte[] data, int offset, TimeZone timeZone) throws IOException {
		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.clear();

		int yearOffset = (data[offset + 3] & 0xC0) / 64;
		int year = getYear(new Date(), yearOffset, timeZone );

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.SECOND, ProtocolUtils.BCD2hex(data[offset]));
		calendar.set(Calendar.MINUTE, ProtocolUtils.BCD2hex(data[offset + 1]));
		calendar.set(Calendar.DAY_OF_MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 3] & 0x3F)));
		calendar.set(Calendar.MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 4] & 0x1F)) - 1);
		calendar.set(Calendar.HOUR_OF_DAY, ProtocolUtils.BCD2hex(data[offset + 2]));

		int yearHigh = ProtocolUtils.BCD2hex(data[offset + 2]);
		int yearLow = ProtocolUtils.BCD2hex(data[offset + 2]);

		return calendar.getTime();

	}

	/** Parse a timestamp, 4 byte */
	public static Date parseTimeStamp(byte[] data, int offset, TimeZone timeZone)
	throws IOException {

		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, ProtocolUtils.BCD2hex(data[offset]));
		calendar.set(Calendar.HOUR_OF_DAY, ProtocolUtils.BCD2hex(data[offset + 1]));
		calendar.set(Calendar.DAY_OF_MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 2] & 0x3F)));
		calendar.set(Calendar.MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 3] & 0x1F)) - 1);

		int yearOffset = (data[offset + 2] & 0xC0) / 64;
		int year = getYear(new Date(), yearOffset, timeZone);

		calendar.set(Calendar.YEAR, year);

		return calendar.getTime();

	}

	/** Build a date, a full date (7 byte) */
	public static byte[] buildDate(Date date, TimeZone timeZone) {

		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.clear();
		calendar.setTime(date);
		byte[] data = new byte[14];

		ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND), data, 0);

		ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE), data, 2);
		ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY), data, 4);

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int year = calendar.get(Calendar.YEAR);

		byte y = ProtocolUtils.hex2BCD(day);
		y |= (year % 4) * 0x40;

		data[6] = (byte) ProtocolUtils.convertHexLSB(y & 0xFF);
		data[7] = (byte) ProtocolUtils.convertHexMSB(y & 0xFF);

		int month = calendar.get(Calendar.MONTH) + 1;
		int dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK);
		int dayOfWeek = 0;

		if (dayOfWeekIndex == Calendar.MONDAY) {
			dayOfWeek = 0;
		}
		if (dayOfWeekIndex == Calendar.TUESDAY) {
			dayOfWeek = 1;
		}
		if (dayOfWeekIndex == Calendar.WEDNESDAY) {
			dayOfWeek = 2;
		}
		if (dayOfWeekIndex == Calendar.THURSDAY) {
			dayOfWeek = 3;
		}
		if (dayOfWeekIndex == Calendar.FRIDAY) {
			dayOfWeek = 4;
		}
		if (dayOfWeekIndex == Calendar.SATURDAY) {
			dayOfWeek = 5;
		}
		if (dayOfWeekIndex == Calendar.SUNDAY) {
			dayOfWeek = 6;
		}

		byte m = ProtocolUtils.hex2BCD(month);
		m |= dayOfWeek * 0x20;

		data[8] = (byte) ProtocolUtils.convertHexLSB(m & 0xFF);
		data[9] = (byte) ProtocolUtils.convertHexMSB(m & 0xFF);


		ProtocolUtils.val2BCDascii(0, data, 10);
		ProtocolUtils
		.val2BCDascii(calendar.get(Calendar.YEAR) - 2000, data, 12);

		return data;

	}

	/**
	 * Must be more precise, up to day, for the moment only months are taken
	 * into consideration.
	 */
	static int getYear(Date today, int offset, TimeZone timeZone ) {

		Calendar c = ProtocolUtils.getCalendar( timeZone );
		c.setTime(today);
		int todayYear = c.get(Calendar.YEAR);
		int offsetYear = 0;

		if ((offset > 0)) {
			if ((todayYear % 4) == 0) {
				offsetYear = todayYear - 4;
			} else {
				if (offset <= todayYear % 4) {
					offsetYear = todayYear - (todayYear % 4);
				} else {
					offsetYear = todayYear - 4 - (todayYear % 4);
				}
			}
		} else { // == 0
			offsetYear = ((todayYear % 4) == 0) ? todayYear : todayYear
					- (todayYear % 4);
		}
		return offsetYear + offset;
	}

	public static byte toBCD(int i) {
		int b0 = i % 10;
		i /= 10;
		int b1 = i % 10;

		byte b = (byte) ((b1 << 4) | b0);

		return b;
	}

	/** split byte stream for token '#' */
	public static ArrayList split(byte[] b, int offset) {
		ArrayList result = new ArrayList();
		ByteArrayOutputStream temp = new ByteArrayOutputStream();

		for (int i = offset; i < b.length; i++) {
			if (b[i] == 0x23) {
				byte[] ba = temp.toByteArray();
				result.add(ba);
				temp.reset();
			} else {
				temp.write(b[i]);
			}
		}
		byte[] ba = temp.toByteArray();
		result.add(ba);

		return result;
	}

	/** Clear hours, minutes and seconds to start at the beginning of the day.
	 */
	static public Calendar clear( Calendar c ){
		c.set( Calendar.SECOND, 0 );
		c.set( Calendar.MINUTE, 0 );
		c.set( Calendar.HOUR_OF_DAY, 0 );
		return c;
	}

	/** Return the number of hours in the day.
	 */
	static public int hoursInDay( Calendar c  ){

		Calendar start = clear( (Calendar)c.clone() );

		Calendar tomorow =(Calendar)start.clone();
		tomorow.add( Calendar.DAY_OF_YEAR, 1 );

		return (int)( tomorow.getTime().getTime() - start.getTime().getTime() )
		/ MILLISECONDS_IN_HOUR;

	}

	/** Special debug stuff */
	public static String toBinaryString(int b) {
		char[] result = {'0', '0', '0', '0', '0', '0', '0', '0'};
		char[] bitArray = Integer.toBinaryString(b & 0xFF).toCharArray();
		System.arraycopy(bitArray, 0, result, 8 - bitArray.length,
				bitArray.length);
		return new String(result);
	}

	/** Special debug stuff */
	public static String toHexaString(int b) {
		String r = Integer.toHexString(b & 0xFF);
		if (r.length() < 2) {
			r = "0" + r;
		}
		return r;
	}

	/** Special debug stuff */
	public static String toHexaString( byte [] b ){
		StringBuffer sb = new StringBuffer();
		if( b == null ) {
			sb.append( " <null> " );
		} else {
			for( int i = 0; i < b.length; i ++  ){
				sb.append( toHexaString( b[i] ) + " " );
			}
		}
		return sb.toString();
	}

	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

}