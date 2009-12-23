package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;

public class AXDRObisCode {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRObisCode() {
	}

	/**
	 * @param obisCode
	 * @return
	 */
	public static OctetString encode(ObisCode obisCode) {
		return OctetString.fromString(obisCode.toString());
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static ObisCode decode(AbstractDataType dataType) {
		ObisCode obisCode = ObisCode.fromString(dataType.getOctetString().stringValue());
		return obisCode;
	}

}
