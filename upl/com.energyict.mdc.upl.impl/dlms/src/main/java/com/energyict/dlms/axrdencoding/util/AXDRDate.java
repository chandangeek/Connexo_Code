package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class AXDRDate {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRDate() {
	}

	private static final int	MILLIS_IN_ONE_SECOND	= 1000;

	/**
	 * @param date
	 * @return
	 */
	public static AbstractDataType encode(Date date) {
		if (date == null) {
			return new NullData();
		} else {
			return new Unsigned32(date.getTime() / MILLIS_IN_ONE_SECOND);
		}
	}

    public static OctetString fromDate(String dateString, TimeZone deviceTimeZone) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        simpleDateFormat.setTimeZone(deviceTimeZone);
        Calendar cal = Calendar.getInstance(deviceTimeZone);
        cal.setTime(simpleDateFormat.parse(dateString));

        byte[] result = new byte[5];
        result[0] = (byte) (cal.get(Calendar.YEAR) >> 8);
        result[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        result[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        result[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        result[4] = getDayOfWeek(cal);

        return OctetString.fromByteArray(result, result.length);
    }

	/**
     * Convert to readable yyyyMMdd string
     */
    public static String toDescription(OctetString date) {
        StringBuilder sb = new StringBuilder();
        byte[] octetStr = date.getOctetStr();
        int year = ((octetStr[0] & 0xFF) << 8) + (octetStr[1] & 0xFF);
        sb.append(year);
        sb.append("/");
        String month = String.valueOf(octetStr[2] & 0xFF);
        sb.append(month.length() == 1 ? ("0" + month) : month);
        sb.append("/");
        String day = String.valueOf(octetStr[3] & 0xFF);
        sb.append(day.length() == 1 ? ("0" + day) : day);
        return sb.toString();
    }

    private static byte getDayOfWeek(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
        return (byte) dayOfWeek;
    }

    /**
	 * @param dataType
	 * @return
	 */
	public static Date decode(AbstractDataType dataType) {
		if ((dataType == null) || (dataType.isNullData())) {
			return null;
		} else {
			return new Date(dataType.longValue() * MILLIS_IN_ONE_SECOND);
		}
	}
}
