package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
 * </pre>
 *
 * @author fbo
 */

public final class PPMUtils {

	private static final int	MILLISECONDS_IN_HOUR	= 60 * 60 * 1000;

	private static final int	BIG_DECIMAL_MAX_LENGTH	= 8;
	private static final int	QUANTITY_MAX_LENGTH		= 8;
	private static final int	BITFIELD_MAX_LENGTH		= 8;
	private static final int	LONG_MAX_LENGTH			= 8;
	private static final int	INTEGER_MAX_LENGTH		= 4;

	private PPMUtils() {}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws ProtocolException
	 */
	public static Long parseLongHexLE(byte[] data, int offset, int length) throws ProtocolException {
		return new Long(ProtocolUtils.getLongLE(data, offset, length));
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws ProtocolException
	 */
	public static Long parseLongHex(byte[] data, int offset, int length) throws ProtocolException {
		return new Long(ProtocolUtils.getLong(data, offset, length));
	}

	/**
	 * @param val
	 * @return
	 */
	public static String buildHexLE(Long val) {
		long lVal = val.longValue();
		byte[] data = new byte[4];
		ProtocolUtils.val2HEXascii((int) lVal & 0xFF, data, 0);
		ProtocolUtils.val2HEXascii((int) (lVal >> 8) & 0xFF, data, 2);
		return new String(data);
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @param unit
	 * @return
	 * @throws ProtocolException
	 */
	public static BigDecimal parseBigDecimal(byte[] data, int offset, int length, Unit unit) throws ProtocolException {
		if (length > BIG_DECIMAL_MAX_LENGTH) {
			throw new ProtocolException("Register, parseBigDecimal, datalength should not exceed " + BIG_DECIMAL_MAX_LENGTH + "!");
		}
        try {
		    BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data, offset, length))));
		    return bd.movePointLeft(Math.abs(unit.getScale()));
        }catch(NumberFormatException e){
            throw new ProtocolException(e);
        }
    }

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @param scale
	 * @param unit
	 * @return
	 * @throws ProtocolException
	 */
	public static Quantity parseQuantity(byte[] data, int offset, int length, BigDecimal scale, Unit unit) throws ProtocolException {
		if (length > QUANTITY_MAX_LENGTH) {
			throw new ProtocolException("Register, parseQuantity, datalength should not exceed " + QUANTITY_MAX_LENGTH + "!");
		}
        try {
            BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLong(data, offset, length))));
            if (scale == null) {
                bd = BigDecimal.ZERO;
            } else {
                bd = bd.multiply(scale);
            }
            return new Quantity(bd, unit);
        }catch(NumberFormatException e){
            throw new ProtocolException(e);
        }
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws ProtocolException
	 */
	public static Long parseBitfield(byte[] data, int offset, int length) throws ProtocolException {
		if (length > BITFIELD_MAX_LENGTH) {
			throw new ProtocolException("Register, parseBitfield, datalength should not exceed " + BITFIELD_MAX_LENGTH + "!");
		}
        try {
		    return new Long(ProtocolUtils.getLong(data, offset, length));
        }catch(NumberFormatException e){
            throw new ProtocolException(e);
        }
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws ProtocolException
	 */
	public static Long parseLong(byte[] data, int offset, int length) throws ProtocolException {
		if (length > LONG_MAX_LENGTH) {
			throw new ProtocolException("Register, parseLong, datalength should not exceed " + LONG_MAX_LENGTH + "!");
		}
        try {
            return new Long(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data, offset, length))));
        }catch(NumberFormatException e){
            throw new ProtocolException(e);
        }
	}

	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 * @throws ProtocolException
	 */
	public static Integer parseInteger(byte[] data, int offset, int length) throws ProtocolException {
		if (length > INTEGER_MAX_LENGTH) {
			throw new ProtocolException("Register, parseInteger, datalength should not exceed " + INTEGER_MAX_LENGTH + "!");
		}
        try {
            return Integer.valueOf(Integer.parseInt(Integer.toHexString(ProtocolUtils.getIntLE(data, offset, length))));
        }catch (NumberFormatException e){
            throw new ProtocolException(e);
        }
	}

	/**
	 * Parse a BCD String from native meter format
	 *
	 * @param data
	 * @return
	 */
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

	/**
	 * Parse a Date (7 byte full date)
	 *
	 * @param data
	 * @param offset
	 * @param timeZone
	 * @return
	 * @throws ProtocolException
	 */
	public static Date parseDate(byte[] data, int offset, TimeZone timeZone) throws ProtocolException {
		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.clear();

		int yearOffset = (data[offset + 3] & 0xC0) / 64;
		int year = getYear(new Date(), yearOffset, timeZone);

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.SECOND, ProtocolUtils.BCD2hex(data[offset]));
		calendar.set(Calendar.MINUTE, ProtocolUtils.BCD2hex(data[offset + 1]));
		calendar.set(Calendar.DAY_OF_MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 3] & 0x3F)));
		calendar.set(Calendar.MONTH, ProtocolUtils.BCD2hex((byte) (data[offset + 4] & 0x1F)) - 1);
		calendar.set(Calendar.HOUR_OF_DAY, ProtocolUtils.BCD2hex(data[offset + 2]));

		return calendar.getTime();

	}

	/**
	 * Parse a timestamp, 4 byte
	 *
	 * @param data
	 * @param offset
	 * @param timeZone
	 * @return
	 * @throws ProtocolException
	 */
	public static Date parseTimeStamp(byte[] data, int offset, TimeZone timeZone) throws ProtocolException {

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

	/**
	 * Build a date, a full date (7 byte)
	 *
	 * @param date
	 * @param timeZone
	 * @return
	 */
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
		ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR) - 2000, data, 12);

		return data;

	}

	/**
	 * Must be more precise, up to day, for the moment only months are taken
	 * into consideration.
	 *
	 * @param today
	 * @param offset
	 * @param timeZone
	 * @return
	 */
	public static int getYear(Date today, int offset, TimeZone timeZone) {

		Calendar c = ProtocolUtils.getCalendar(timeZone);
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
			offsetYear = ((todayYear % 4) == 0) ? todayYear : todayYear - (todayYear % 4);
		}
		return offsetYear + offset;
	}

	public static byte toBCD(int i) {
		int b0 = i % 10;
		i /= 10;
		int b1 = i % 10;
		return (byte) ((b1 << 4) | b0);
	}

	/**
	 * Split byte stream for token '#'
	 *
	 * @param b
	 * @param offset
	 * @return
	 */
	public static List split(byte[] b, int offset) {
		List result = new ArrayList();
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

	/**
	 * Clear hours, minutes and seconds to start at the beginning of the day.
	 *
	 * @param c
	 * @return
	 */
	public static Calendar clear(Calendar c) {
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		return c;
	}

	/**
	 * Return the number of hours in the day.
	 *
	 * @param c
	 * @return
	 */
	public static int hoursInDay(Calendar c) {
		Calendar start = clear((Calendar) c.clone());
		Calendar tomorow = (Calendar) start.clone();
		tomorow.add(Calendar.DAY_OF_YEAR, 1);
		return (int) (tomorow.getTime().getTime() - start.getTime().getTime()) / MILLISECONDS_IN_HOUR;
	}

	/**
	 * Special debug stuff
	 *
	 * @param b
	 * @return
	 */
	public static String toBinaryString(int b) {
		char[] result = { '0', '0', '0', '0', '0', '0', '0', '0' };
		char[] bitArray = Integer.toBinaryString(b & 0xFF).toCharArray();
		System.arraycopy(bitArray, 0, result, 8 - bitArray.length, bitArray.length);
		return new String(result);
	}

	/**
	 * Special debug stuff
	 *
	 * @param b
	 * @return
	 */
	public static String toHexaString(int b) {
		String r = Integer.toHexString(b & 0xFF);
		if (r.length() < 2) {
			r = "0" + r;
		}
		return r;
	}

	/**
	 * Special debug stuff
	 *
	 * @param b
	 * @return
	 */
	public static String toHexaString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		if (b == null) {
			sb.append(" <null> ");
		} else {
			for (int i = 0; i < b.length; i++) {
				sb.append(toHexaString(b[i]) + " ");
			}
		}
		return sb.toString();
	}

	/**
	 * @param value
	 * @return
	 */
	public static long hex2dec(byte value) {
		return Long.parseLong(Long.toHexString(value & 0xFF));
	}

	/**
	 * Store a array of bytes to a file
	 *
	 * @param data
	 * @param fileName
	 */
	public static void toFile(byte[] data, String fileName) {
		try {
			FileOutputStream writer = new FileOutputStream(fileName);
			writer.write(data);
			writer.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Read a array of bytes from a file
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static byte[] fromFile(String fileName) throws IOException {
		File file = new File(fileName);
		if (file.length() > Integer.MAX_VALUE) {
			throw new IOException("Filesize to big. [" + file.length() + " > " + Integer.MAX_VALUE + "]");
		}
		byte[] data = new byte[(int) file.length()];
		FileInputStream reader = new FileInputStream(fileName);
		reader.read(data);
		reader.close();
		return data;
	}

}