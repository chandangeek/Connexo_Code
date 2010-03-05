/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;

/**
 * Utility class
 * 
 * @author gna
 * @since 5-mrt-2010
 *
 */
public class DL220Utils {
	
	public static final String MINUTEN_STR = "Minuten";
	public static final String HOURES_STR = "Stunden";
	
	/**
	 * Convert the given quantity(String[]) to the amount of seconds. If the unit part is different from {@value #MINUTEN_STR}
	 * or {@value #HOURES_STR}, then we assume the unit is in seconds.
	 * 
	 * @param quantity 
	 * 			- the quantity containing a value and unit
	 * 
	 * @return the amount of seconds form the quantity
	 */
	public static int convertQuantityToSeconds(String[] quantity){
		int seconden = 0;
		if(quantity[DLObject.unitIndex].equalsIgnoreCase(MINUTEN_STR)){
			seconden = Integer.parseInt(quantity[DLObject.valueIndex]) * 60;
		} else if(quantity[DLObject.unitIndex].equalsIgnoreCase(HOURES_STR)){
			seconden = Integer.parseInt(quantity[DLObject.valueIndex]) * 60 * 60;
		} else {
			seconden = Integer.parseInt(quantity[DLObject.valueIndex]);
		}
		return seconden;
	}

}
