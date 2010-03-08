/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.protocol.ProtocolUtils;
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

	/**
	 * Return a complete record
	 * 
	 * @param rawData
	 * 			- the compete rawData
	 * 
	 * @param offset
	 * 			- the offset to return the next index
	 * 
	 * @return a complete record
	 */
	public static String getNextRecord(String rawData, int offset) {
		int beginIndex = rawData.indexOf("(", offset);
		int endIndex = beginIndex;
		for(int i = 0; i < 9; i++){
			endIndex = rawData.indexOf(")", endIndex) + 1;
		}
		return rawData.substring(beginIndex, endIndex);
	}
	
	/**
	 * Return the text from in between the brackets. The index indicates which bracket-pair to return.<br>
	 * ex: text = (one)(two)(three)(four)(five) - index = 2<br>
	 * this will return 'three'
	 * 
	 * @param text
	 * 			- the String with the bracket pairs
	 * 
	 * @param index
	 * 			- the bracket pair-index (zero-based)
	 * 
	 * @return
	 * 			- the text between the brackets
	 */
	public static String getTextBetweenBracketsStartingFrom(String text, int index){
		int beginIndex = 0;
		int endIndex = 0;
		for(int i = 0; i <= index; i++){
			beginIndex = text.indexOf("(", beginIndex) + 1;
			if(beginIndex == 0){
				throw new IllegalArgumentException("Could not return the request text, index to large(" + index + ").");
			}
		}
		endIndex = text.indexOf(")", beginIndex);
		return ProtocolUtils.stripBrackets(text.substring(beginIndex, endIndex));
	}

}
