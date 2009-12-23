package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Structure;

public class AXDRTimeDuration {

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
		Structure structure = new Structure();
		structure.addDataType(new Integer32(timeDuration.getCount()));
		structure.addDataType(new Integer32(timeDuration.getTimeUnitCode()));
		return structure;
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static TimeDuration decode(AbstractDataType dataType) {
		int count = dataType.getStructure().getDataType(0).intValue();
		int timeUnitCode = dataType.getStructure().getDataType(1).intValue();
		return new TimeDuration(count,timeUnitCode);
	}

}
