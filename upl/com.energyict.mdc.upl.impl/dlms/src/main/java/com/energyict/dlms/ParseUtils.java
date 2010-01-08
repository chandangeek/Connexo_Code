package com.energyict.dlms;

import com.energyict.obis.ObisCode;

/**
 * DLMS ParseUtils
 *
 */
public final class ParseUtils {

	/**
	 * Hide this static util class
	 */
	private ParseUtils() {
		// Hide this static util class
	}

	/**
	 * @param obisCode
	 * @return
	 */
	public static boolean isObisCodeAbstract(ObisCode obisCode) {
		return (obisCode.getA()==0) && (obisCode.getB()==0);
	}

	/**
	 * @param obisCode
	 * @return
	 */
	public static boolean isObisCodeCumulative(ObisCode obisCode) {
		// no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin)
		return (obisCode.getA()!=0) && (obisCode.getC()!=0) && ((obisCode.getD()==8) || (obisCode.getD()==17) || (obisCode.getD()==18));
	}

	/**
	 * @param obisCode
	 * @return
	 */
	public static boolean isObisCodeChannelIntervalStatus(ObisCode obisCode) {
		// no abstract code AND d field time integral 1, 7 or 8 These time integrals specify values from first start of measurement (origin)
		return (obisCode.getA()==0) && (obisCode.getB()==0) && (obisCode.getC()==96) && (obisCode.getD()==60) && (obisCode.getE()>0) && (obisCode.getF()==0);
	}

	/**
	 * @param obisCode
	 * @return
	 */
	public static boolean isElectricityObisCode(ObisCode obisCode){
		return ((obisCode.getA() == 1) && (obisCode.getB() >= 0) && (obisCode.getB() <= 64));
	}

	/**
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static byte[] concatArray(byte[] array1, byte[] array2) {
		if ((array1 == null) && (array2 == null)) {
			return null;
		} else if (array1 == null) {
			return array2;
		} else if (array2 == null) {
			return array1;
		}
		byte[] newArray = new byte[array1.length+array2.length];
		System.arraycopy(array1, 0, newArray, 0, array1.length);
		System.arraycopy(array2, 0, newArray, array1.length,array2.length);
		return newArray;
	}

	/**
	 * @param code
	 * @return
	 */
	public static boolean isObisCode(String code) {
		try {
			ObisCode.fromString(code).toString();
			return true;
		}
		catch(NumberFormatException e) {
			return false;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
	}


}
