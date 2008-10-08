package com.energyict.dlms.axrdencoding.util;

import java.util.*;

import com.energyict.dlms.axrdencoding.*;

public class AXDRTimeZone {
	
	static public OctetString encode(TimeZone timeZone) {
		return OctetString.fromString(timeZone.getID());
	}
	
	static public TimeZone decode(AbstractDataType dataType) {
		TimeZone timeZone = TimeZone.getTimeZone(dataType.getOctetString().stringValue());
		return timeZone;
	}	
	
	static public void main(String[] args) {
		TimeZone timeZone = TimeZone.getTimeZone("Europe/Brussels");
		OctetString o = encode(timeZone);
		System.out.println(o);
		System.out.println(decode(o).getID());

		System.out.println(decode(o));
	}
}
