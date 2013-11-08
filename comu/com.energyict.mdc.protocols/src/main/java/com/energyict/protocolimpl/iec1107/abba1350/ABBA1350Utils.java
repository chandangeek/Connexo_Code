/**
 * ABBA1350Utils.java
 * 
 * Created on 20-nov-2008, 11:15:48 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.abba1350;

/**
 * @author jme
 *
 */
public class ABBA1350Utils {

	private static final int DEBUG = 0;
	
	public static String getXMLAttributeValue(String attribute, String xmlstring) {
		return getXMLAttributeValue(attribute, xmlstring, true);
	}
	
	public static String getXMLAttributeValue(String attribute, String xmlstring, boolean isCaseSensitive) {
		if (xmlstring == null) return null;
		if (attribute == null) return null;
		if (xmlstring.length() < 7) return null;
		if (attribute.length() < 1) return null;
		
		String xmlLow = xmlstring;
		attribute.trim();
		if (!isCaseSensitive) {
			xmlLow = xmlstring.toLowerCase();
			attribute = attribute.toLowerCase();
		}
		
		String starttag = "<" + attribute + ">";
		String endtag = "</" + attribute + ">";
		int starttagpos = xmlLow.indexOf(starttag);
		int endtagpos = xmlLow.indexOf(endtag);
		int starttaglen = starttag.length();
		
		if (starttagpos == -1) return null;
		if (endtagpos == -1) return null;
		if ((starttagpos + starttaglen) > endtagpos) return null;
		
		String value = xmlstring.substring(starttagpos + starttaglen, endtagpos).trim(); 
		if (containsCharacters(value, "<>")) return null;

		return value;
	}

	public static boolean containsCharacters(String inputstring, String characterlist) {
		char chr;
		for (int i = 0; i < characterlist.length(); i++) {
			chr = characterlist.charAt(i);
			//if (inputstring.contains(String.valueOf(chr))) return true;
			if (inputstring.indexOf(String.valueOf(chr)) != -1) return true;
		}
		return false;
	}
	
	public static boolean containsOnlyTheseCharacters(String inputstring, String characterlist) {
		char chr;
		for (int i = 0; i < inputstring.length(); i++) {
			chr = inputstring.charAt(i);
			//if (!characterlist.contains(String.valueOf(chr))) return false;
			if (inputstring.indexOf(String.valueOf(chr)) == -1) return false;
		}
		return true;
	}

	public static String cleanAttributeValue(String attributeValue) {
		final char TAB = 0x09;
		final char LF = 0x0A;
		final char CR = 0x0D;
		final char SPACE = 0x20;
		
		boolean isValidChar = true;
		String r = "";

		for (int i = 0; i < attributeValue.length(); i ++) {
			isValidChar = true;
			if (attributeValue.charAt(i) == SPACE) isValidChar = false;
			if (attributeValue.charAt(i) == CR) isValidChar = false;
			if (attributeValue.charAt(i) == LF) isValidChar = false;
			if (attributeValue.charAt(i) == TAB) isValidChar = false;
			if (isValidChar) r += attributeValue.charAt(i);
		}
		return r;
	}
	
}
