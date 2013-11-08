/**
 * A1440Utils.java
 * 
 * Created on 20-nov-2008, 11:15:48 by jme
 * 
 */
package com.energyict.protocolimpl.iec1107.a1440;

/**
 * @author jme
 *
 */
public final class A1440Utils {

	private static final int MIN_ATTRIBUTE_LENGTH = 1;
	private static final int MIN_XML_LENGTH = 7;

	private static final char TB = 0x09;
	private static final char LF = 0x0A;
	private static final char CR = 0x0D;
	private static final char SP = 0x20;

	private A1440Utils() {}

	public static String getXMLAttributeValue(String attribute, String xmlstring) {
		return getXMLAttributeValue(attribute, xmlstring, true);
	}

	public static String getXMLAttributeValue(String attribute, String xmlstring, boolean isCaseSensitive) {
		if ((xmlstring == null) || (attribute == null)){
			return null;
		}
		if ((xmlstring.length() < MIN_XML_LENGTH) || (attribute.length() < MIN_ATTRIBUTE_LENGTH)) {
			return null;
		}

		String xmlLow = xmlstring;
		String attr = attribute.trim();
		if (!isCaseSensitive) {
			xmlLow = xmlstring.toLowerCase();
			attr = attribute.toLowerCase();
		}

		String starttag = "<" + attr + ">";
		String endtag = "</" + attr + ">";
		int starttagpos = xmlLow.indexOf(starttag);
		int endtagpos = xmlLow.indexOf(endtag);
		int starttaglen = starttag.length();

		if ((starttagpos == -1) || (endtagpos == -1) || ((starttagpos + starttaglen) > endtagpos)) {
			return null;
		}

		String value = xmlstring.substring(starttagpos + starttaglen, endtagpos).trim();
		if (containsCharacters(value, "<>")) {
			return null;
		}

		return value;
	}

	public static boolean containsCharacters(String inputstring, String characterlist) {
		char chr;
		for (int i = 0; i < characterlist.length(); i++) {
			chr = characterlist.charAt(i);
			if (inputstring.indexOf(String.valueOf(chr)) != -1) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsOnlyTheseCharacters(String inputstring, String characterlist) {
		char chr;
		for (int i = 0; i < inputstring.length(); i++) {
			chr = inputstring.charAt(i);
			if (inputstring.indexOf(String.valueOf(chr)) == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes the following characters from a given string:
	 * <lu>
	 * <li>TAB</li>
	 * <li>LINE FEED</li>
	 * <li>CARIAGE RETURN</li>
	 * <li>SPACE</li>
	 * </lu>
	 * @param attributeValue
	 * @return The cleaned string
	 */
	public static String cleanAttributeValue(String attributeValue) {
		String returnValue = "";
		for (int i = 0; i < attributeValue.length(); i ++) {
			if (isValidChar(attributeValue.charAt(i))) {
				returnValue += attributeValue.charAt(i);
			}
		}
		return returnValue;
	}

	/**
	 * Check is a given character is valid or not. This is a list of invalid characters:
	 * <lu>
	 * <li>TAB</li>
	 * <li>LINE FEED</li>
	 * <li>CARIAGE RETURN</li>
	 * <li>SPACE</li>
	 * </lu>
	 * @param character
	 * @return
	 */
	private static boolean isValidChar(char character) {
		switch (character) {
		case TB: return false;
		case LF: return false;
		case CR: return false;
		case SP: return false;
		default: return true;
		}
	}

}
