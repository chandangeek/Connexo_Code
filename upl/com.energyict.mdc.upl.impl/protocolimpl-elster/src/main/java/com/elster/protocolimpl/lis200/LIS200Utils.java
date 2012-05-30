package com.elster.protocolimpl.lis200;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;

public class LIS200Utils {

    /**
     * @param objectAddress is the Lis200 address to check
     * @return true if objectAddress is a syntactically correct address,
     *         otherwise returns false
     */
    public static boolean isValidLis200Address(String objectAddress) {
        String[] addressPart = objectAddress.split(":");
        if (addressPart.length != 2)
            return false;

        try {
            Integer.parseInt(addressPart[0]);
        } catch (Exception e) {
            return false;
        }

        String[] addressPart2 = addressPart[1].split("[.]");
        if (addressPart2.length != 2)
            return false;

        return (addressPart2[0].length() != 0) && (addressPart2[1].length() != 0);
    }

    /**
     * Return a complete record
     *
     * @param rawData                 - the compete rawData
     * @param offset                  - the offset to return the next index
     * @param numberOfObjectsInRecord - the number of objects in one record
     * @return end of record
     */
    public static int getNextRecord(String rawData, int offset,
                                    int numberOfObjectsInRecord) {

        int result = offset;
        for (int i = 0; i < numberOfObjectsInRecord; i++) {
            result = rawData.indexOf(")", result) + 1;
        }
        return result;
    }

    /**
     * Return the text from in between the brackets. The index indicates which
     * bracket-pair to return.<br>
     * ex: text = (one)(two)(three)(four)(five) - index = 2<br>
     * this will return 'three'
     *
     * @param text  - the String with the bracket pairs
     * @param index - the bracket pair-index (zero-based)
     * @return - the text between the brackets
     */
    public static String getTextBetweenBracketsFromIndex(String text, int index) {
        int beginIndex = 0;
        int endIndex;
        for (int i = 0; i <= index; i++) {
            beginIndex = text.indexOf("(", beginIndex) + 1;
            if (beginIndex == 0) {
                throw new IllegalArgumentException(
                        "Could not return the request text, index to large("
                                + index + ").");
            }
        }
        endIndex = text.indexOf(")", beginIndex);
        return ProtocolUtils
                .stripBrackets(text.substring(beginIndex, endIndex));
    }

    /**
     * Convert the given String to the respective {@link Unit}.<br>
     * Implemented units:<br>
     * <li> {@link BaseUnit#CUBICMETER} <li> {@link BaseUnit#WATTHOUR} <li>
     * {@link BaseUnit#WATT} <br>
     * <br>
     * The last two can have a scaler of 3 when 'k' is added in the string
     *
     * @param strUnit - the given strUnit
     * @return the Unit
     */
    public static Unit getUnitFromString(String strUnit) {
        int scaler = 0;
        if (strUnit.equalsIgnoreCase("m3")) {
            return Unit.get(BaseUnit.CUBICMETER);
        } else if (strUnit.equalsIgnoreCase("bar")) {
            return Unit.get(BaseUnit.BAR);
        } else if (strUnit.equalsIgnoreCase("{F") ||
                strUnit.equalsIgnoreCase("\u00B0F") ||
                strUnit.equalsIgnoreCase("F")) {
            return Unit.get(BaseUnit.FAHRENHEIT);
        } else if (strUnit.contains("Wh")) {
            if (strUnit.contains("k")) {
                scaler = 3;
            }
            if (strUnit.contains("M")) {
                scaler = 6;
            }
            return Unit.get(BaseUnit.WATTHOUR, scaler);
        } else if (strUnit.contains("W")) {
            if (strUnit.contains("k")) {
                scaler = 3;
            }
            if (strUnit.contains("M")) {
                scaler = 6;
            }
            return Unit.get(BaseUnit.WATT, scaler);
        } else if ((strUnit.contains("m3|h")) ||
                (strUnit.contains("m3/h")) ||
                (strUnit.contains("m3:h"))) {
            return Unit.get(BaseUnit.CUBICMETERPERHOUR);
        } else if (strUnit.equals("K") ||
                strUnit.equals("\u00B0K")) {
            return Unit.get(BaseUnit.KELVIN);
        } else {
            if (strUnit.endsWith("C") || strUnit.endsWith("c")) {
                switch (strUnit.length()) {
                    case 1:
                        return Unit.get(BaseUnit.DEGREE_CELSIUS);
                    case 2:
                        if ((strUnit.charAt(0) == '{') || (strUnit.charAt(0) == '\u00B0')) {
                            return Unit.get(BaseUnit.DEGREE_CELSIUS);
                        }
                }
            }
        }
        return Unit.getUndefined();
    }

}
