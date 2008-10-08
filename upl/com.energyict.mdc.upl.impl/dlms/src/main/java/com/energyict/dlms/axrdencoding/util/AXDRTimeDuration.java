package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.axrdencoding.*;

public class AXDRTimeDuration {

	
	static public Structure encode(TimeDuration timeDuration) {
		Structure structure = new Structure();
		structure.addDataType(new Integer32(timeDuration.getCount()));
		structure.addDataType(new Integer32(timeDuration.getTimeUnitCode()));
		return structure;
	}
	
	static public TimeDuration decode(AbstractDataType dataType) {
		int count = dataType.getStructure().getDataType(0).intValue();
		int timeUnitCode = dataType.getStructure().getDataType(1).intValue();
		return new TimeDuration(count,timeUnitCode);
	}
	
	static public void main(String[] args) {
		
		TimeDuration o = new TimeDuration(7,3);
		System.out.println(o);
		Structure s = encode(o);
		System.out.println(decode(s));
	}
	
}
