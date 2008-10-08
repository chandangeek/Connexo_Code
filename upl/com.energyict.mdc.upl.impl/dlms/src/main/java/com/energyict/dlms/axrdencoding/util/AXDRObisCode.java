package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;

public class AXDRObisCode {
	
	static public OctetString encode(ObisCode obisCode) {
		return OctetString.fromString(obisCode.toString());
	}
	
	static public ObisCode decode(AbstractDataType dataType) {
		ObisCode obisCode = ObisCode.fromString(dataType.getOctetString().stringValue());
		return obisCode;
	}	
	
	static public void main(String[] args) {
		ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
		OctetString o = encode(obisCode);
		System.out.println(o);
		System.out.println(decode(o));
	}
}
