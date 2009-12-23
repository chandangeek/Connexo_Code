package com.energyict.dlms.axrdencoding.util;

import java.util.Date;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Unsigned32;

public class AXDRDate {

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
