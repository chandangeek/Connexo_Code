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
	static public boolean isObisCodeChannelIntervalStatus(ObisCode obisCode) {
		// no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin) 
		return (obisCode.getA()==0) && (obisCode.getB()==0) && (obisCode.getC()==96) && (obisCode.getD()==60) && (obisCode.getE()>0) && (obisCode.getF()==0);
	}
	
	static public byte[] concatArray(byte[] array1, byte[] array2) {
		if ((array1==null) && (array2==null))
			return null;
		if (array1==null)
			return array2;
		if (array2==null)
			return array1;
		byte[] newArray = new byte[array1.length+array2.length];
		System.arraycopy(array1, 0, newArray, 0, array1.length);
		System.arraycopy(array2, 0, newArray, array1.length,array2.length);
		return newArray;
	}
	
	static public boolean isObisCode(String code) {
		try {
			ObisCode o = ObisCode.fromString(code);
			return true;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
	}
	

}
