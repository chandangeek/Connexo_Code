/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;

import java.io.IOException;

/**
 * Utility class
 * 
 * @author gna
 * @since 5-mrt-2010
 *
 */
public class DL220Utils {
	
	/** <i>Minuten</i> unit */
	public static final String MINUTEN_STR = "Minuten";
	/** <i>Stunden</i> unit */
	public static final String HOURES_STR = "Stunden";
	
	private static final String OPEN_BRACKET = "[(]";
	private static final String CLOSE_BRACKET = "[)]";
	
	/**
	 * Convert the given quantity(String[]) to the amount of seconds. If the unit part is different from {@link #MINUTEN_STR}
	 * or {@link #HOURES_STR}, then we assume the unit is in seconds.
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
	 * @param numberOfObjectsInRecord
	 * 			- the number of objects in one record
	 * 
	 * @return a complete record
	 */
	public static String getNextRecord(String rawData, int offset, int numberOfObjectsInRecord) {
		int beginIndex = rawData.indexOf("(", offset);
		int endIndex = beginIndex;
		for(int i = 0; i < numberOfObjectsInRecord; i++){
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
	public static String getTextBetweenBracketsFromIndex(String text, int index){
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

	/**
	 * Count the number of objects in the given objectString
	 * 
	 * @param capturedObjects
	 * 			- the objectString
	 * 
	 * @return the number of objects
	 * 
	 * @throws IOException if the number of open- and closed brackets doesn't match
	 */
	public static int getNumberOfObjects(String capturedObjects) throws IOException {
		int numberOfOpen = countNumberOfSameChars(capturedObjects, OPEN_BRACKET, 1);
		int numberOfClosed = countNumberOfSameChars(capturedObjects, CLOSE_BRACKET, 1);
		if(numberOfOpen == numberOfClosed){
			return numberOfOpen;
		} else {
			 throw new IOException("Number of open brackets does not match the number of closed brackets (" + numberOfOpen + " - " + numberOfClosed + ")");
		}
	}

	/**
	 * Count the number of occurrences of the regular expression in the given text
	 * 
	 * @param text
	 * 			- the text to search in
	 * 
	 * @param regex
	 * 			- the regular expression to match
	 * 
	 * @param sizeOfRegex
	 * 			- normally the size of the regex is <code>regex.Length()</code>, but in some cases the regex contains brackets to define the pattern (ex. <code>"[(]"</code>)
	 * 
	 * @return the number of occurrences
	 */
	public static int countNumberOfSameChars(String text, String regex, int sizeOfRegex){
		int numberOfOccurences = (text.length() - text.replaceAll(regex, "").length()) / sizeOfRegex;
		return numberOfOccurences;
	}
	
	/**
	 * Convert the given String to the respective {@link Unit}.<br>
	 * Implemented units:<br>
	 * <li> {@link BaseUnit#CUBICMETER}
	 * <li> {@link BaseUnit#WATTHOUR}
	 * <li> {@link BaseUnit#WATT}
	 * <br><br>
	 * The last two can have a scaler of 3 when 'k' is added in the string
	 *  
	 * @param strUnit
	 * 			- the given strUnit
	 * 
	 * @return the Unit
	 */
	public static Unit getUnitFromString(String strUnit){
		int scaler = 0;
		if(strUnit.equalsIgnoreCase("m3")){
			return Unit.get(BaseUnit.CUBICMETER);
		} else if (strUnit.indexOf("Wh") > -1){
			scaler = (strUnit.indexOf("k") > -1)?3:0;
			return Unit.get(BaseUnit.WATTHOUR, scaler);
		} else if (strUnit.indexOf("W") > -1){
			scaler = (strUnit.indexOf("k") > -1)?3:0;
			return Unit.get(BaseUnit.WATT, scaler);
		} else if (strUnit.indexOf("m3|h") > -1) {
			return Unit.get(BaseUnit.CUBICMETERPERHOUR);
		} else {
			return Unit.getUndefined();
		}
	}
}
