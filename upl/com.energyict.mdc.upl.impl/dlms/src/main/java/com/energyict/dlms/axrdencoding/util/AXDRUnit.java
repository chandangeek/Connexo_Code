package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;

public final class AXDRUnit {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRUnit() {
	}

	public static OctetString encode(Unit unit) {
		return unit != null ? OctetString.fromString(unit.dbString()) : null;
	}

	public static Unit decode(AbstractDataType dataType) {
		if ((dataType == null) || (dataType.isNullData())) {
			return null;
		} else {
			String unitAsString = dataType.getOctetString().stringValue();
			if ((unitAsString == null) || (unitAsString.isEmpty())) {
				return null;
			} else {
				return Unit.fromDb(unitAsString);
			}
		}
	}

}
