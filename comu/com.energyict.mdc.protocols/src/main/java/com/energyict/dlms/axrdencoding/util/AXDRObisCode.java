/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

public final class AXDRObisCode {

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
			return (obisCode == null) ? null : OctetString.fromString(obisCode.toString());
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static ObisCode decode(AbstractDataType dataType) {
		if ((dataType == null) || (dataType.isNullData())) {
			return null;
		} else {
			return ObisCode.fromString(dataType.getOctetString().stringValue());
		}
	}

}
