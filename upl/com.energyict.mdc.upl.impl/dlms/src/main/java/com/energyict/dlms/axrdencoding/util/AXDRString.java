package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;

public class AXDRString {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRString() {
	}

	/**
	 * @param string
	 * @return
	 */
	public AbstractDataType encode(String string) {
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
	public String decode(AbstractDataType dataType) {
		if (dataType.isNullData()) {
			return null;
		} else {
			return dataType.getOctetString().stringValue();
		}
	}

}
