/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

import com.energyict.mdc.common.Unit;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

public final class AXDRUnit {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRUnit() {
	}

	/**
	 * @param unit
	 * @return
	 */
	public static OctetString encode(Unit unit) {
		return unit != null ? OctetString.fromString(unit.dbString()) : null;
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static Unit decode(AbstractDataType dataType) {
		if ((dataType == null) || (dataType.isNullData())) {
			return null;
		} else {
			String unitAsString = dataType.getOctetString().stringValue();
			if ((unitAsString == null) || (unitAsString.length() == 0)) {
				return null;
			} else {
				return Unit.fromDb(unitAsString);
			}
		}
	}

}
