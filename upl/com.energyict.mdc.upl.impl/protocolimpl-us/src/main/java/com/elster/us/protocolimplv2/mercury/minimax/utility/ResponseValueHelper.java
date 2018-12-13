package com.elster.us.protocolimplv2.mercury.minimax.utility;

import com.energyict.protocol.exception.CommunicationException;

import java.math.BigDecimal;

/**
 * Helps to handle response values from the mercury device
 *
 * @author James Fox
 */
public final class ResponseValueHelper {

    private ResponseValueHelper() {}

    /**
     * Determines if the value is a string value (in the mercury spec, this is represented
     * by a string wrapped in opening and closing speech marks e.g. "value")
     * @param str The string to check
     * @return true if this is a string value, false otherwise
     */
    public static boolean isStringValue(String str) {
        if (str == null) {
            throw new IllegalArgumentException("String for check cannot be null");
        }
        return str.startsWith("\"") && str.endsWith("\"");
    }

    /**
     * Gets the string value from the string
     * @param str The string to extract the value from
     * @return the value if this represents a string value, the original string otherwise
     */
    public static String getStringValue(String str) {
        if (str == null) {
            throw new IllegalArgumentException("String for check cannot be null");
        }
        if (isStringValue(str)) {
            return str.substring(1, str.length()-1);
        }
        return str;
    }

    /**
     * Gets the numeric value from the string
     * @param str The string from which to extract the numeric value
     * @return a {@link BigDecimal} representation of the value
     */
    public static BigDecimal getNumericValue(String str) {

        if (str == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }

        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException nfe) {
            throw CommunicationException.numberFormatException(nfe, "numericValue", str);
        }
    }
}
