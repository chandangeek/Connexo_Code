package com.energyict.dlms.axrdencoding.util;

import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

public class AXDRTimeZone {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRTimeZone() {
	}

	/**
	 * @param timeZone
	 * @return
	 */
	public static OctetString encode(TimeZone timeZone) {
		return OctetString.fromString(timeZone.getID());
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static TimeZone decode(AbstractDataType dataType) {
		TimeZone timeZone = TimeZone.getTimeZone(dataType.getOctetString().stringValue());
		return timeZone;
	}

}
