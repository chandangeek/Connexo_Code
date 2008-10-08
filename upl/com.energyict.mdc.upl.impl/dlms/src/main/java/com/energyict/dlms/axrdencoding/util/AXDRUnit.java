package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;

public class AXDRUnit {

	
	static public OctetString encode(Unit unit) {
		return OctetString.fromString(unit.dbString());
	}
	
	static public Unit decode(AbstractDataType dataType) {
		Unit unit = Unit.fromDb(dataType.getOctetString().stringValue());
		return unit;
	}	
	
	static public void main(String[] args) {
		Unit unit = Unit.get("kWh");
		OctetString o = encode(unit);
		System.out.println(o);
		System.out.println(decode(o));
	}
}
