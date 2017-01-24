package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

import java.util.TimeZone;

public final class AXDRTimeZone {

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
		return (timeZone == null) ? null : OctetString.fromString(timeZone.getID());
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static TimeZone decode(AbstractDataType dataType) {
		return ((dataType == null) || (!dataType.isOctetString())) ? null : TimeZone.getTimeZone(dataType.getOctetString().stringValue());
	}

}
