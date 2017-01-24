package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Structure;
import com.elster.jupiter.time.TimeDuration;

public final class AXDRTimeDuration {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRTimeDuration() {
	}

	/**
	 * @param timeDuration
	 * @return
	 */
	public static Structure encode(TimeDuration timeDuration) {
		if (timeDuration != null) {
			Structure structure = new Structure();
			structure.addDataType(new Integer32(timeDuration.getCount()));
			structure.addDataType(new Integer32(timeDuration.getTimeUnitCode()));
			return structure;
		} else {
			return null;
		}
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static TimeDuration decode(AbstractDataType dataType) {
		if ((dataType != null) && (dataType.isStructure())) {
			int count = dataType.getStructure().getDataType(0).intValue();
			int timeUnitCode = dataType.getStructure().getDataType(1).intValue();
			return new TimeDuration(count, timeUnitCode);
		} else {
			return null;
		}
	}

}
