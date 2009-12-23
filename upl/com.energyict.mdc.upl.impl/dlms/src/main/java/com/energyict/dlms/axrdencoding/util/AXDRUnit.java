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

	/**
	 * @param unit
	 * @return
	 */
	public static OctetString encode(Unit unit) {
		return OctetString.fromString(unit.dbString());
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static Unit decode(AbstractDataType dataType) {
		return Unit.fromDb(dataType.getOctetString().stringValue());
	}

}
