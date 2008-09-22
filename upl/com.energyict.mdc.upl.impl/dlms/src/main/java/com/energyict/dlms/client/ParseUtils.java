package com.energyict.dlms.client;

import com.energyict.obis.ObisCode;

public class ParseUtils {

	static public boolean isObisCodeAbstract(ObisCode obisCode) {
		return (obisCode.getA()==0) && (obisCode.getB()==0);
	}
	static public boolean isObisCodeCumulative(ObisCode obisCode) {
		// no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin) 
		return (obisCode.getA()!=0) && (obisCode.getC()!=0) && ((obisCode.getD()==8) || (obisCode.getD()==17) || (obisCode.getD()==18));
	}
}
