/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;

public final class AXDRString {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRString() {
	}

	/**
	 * @param string
	 * @return
	 */
	public static AbstractDataType encode(String string) {
		if (string == null) {
			return new NullData();
		} else {
			return OctetString.fromString(string);
		}
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static String decode(AbstractDataType dataType) {
		if ((dataType == null) || dataType.isNullData()) {
			return null;
		} else {
			return dataType.getOctetString().stringValue();
		}
	}

}
