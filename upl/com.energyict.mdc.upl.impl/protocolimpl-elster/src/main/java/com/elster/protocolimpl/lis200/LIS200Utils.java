package com.elster.protocolimpl.lis200;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;

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
        if (addressPart.length != 2) {
            return false;
        }

        try {
            Integer.parseInt(addressPart[0]);
        } catch (Exception e) {
            return false;
        }

        String[] addressPart2 = addressPart[1].split("[.]");
        if (addressPart2.length != 2) {
            return false;
        }

        return (!addressPart2[0].isEmpty()) && (!addressPart2[1].isEmpty());
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
     * @param unitStr - the given strUnit
     * @return the Unit
     */
    public static Unit getUnitFromString(String unitStr) {
        String strUnit = unitStr.trim();
        int scaler = 0;
        if ("m3".equalsIgnoreCase(strUnit)) {
            return Unit.get(BaseUnit.CUBICMETER);
        } else if ("bar".equalsIgnoreCase(strUnit)) {
            return Unit.get(BaseUnit.BAR);
        } else if ("{F".equalsIgnoreCase(strUnit) ||
                "\u00B0F".equalsIgnoreCase(strUnit) ||
                "F".equalsIgnoreCase(strUnit)) {
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
        } else if ("K".equals(strUnit) || "\u00B0K".equals(strUnit)) {
            return Unit.get(BaseUnit.KELVIN);
        } else {
            if (strUnit.endsWith("C") || strUnit.endsWith("c")) {
                switch (strUnit.length()) {
                    case 1:
                        return Unit.get(BaseUnit.DEGREE_CELSIUS);
                    case 2:
                        char degreeChar = new String("\u00B0").charAt(0);
                        if ((strUnit.charAt(0) == '{') || (strUnit.charAt(0) == degreeChar)) {
                            return Unit.get(BaseUnit.DEGREE_CELSIUS);
                        }
                        break;
                }
                if ("\u00B0c".equalsIgnoreCase(strUnit)) {
                    return Unit.get(BaseUnit.DEGREE_CELSIUS);
                }
            }
        }
        return Unit.getUndefined();
    }

    public static PropertySpec propertySpec(String name, boolean required) {
        return new LIS200AddressPropertySpec(name, required);
    }

    private static class LIS200AddressPropertySpec implements PropertySpec {
        private final String name;
        private final boolean required;

        private LIS200AddressPropertySpec(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDisplayName() {
            return this.getName();
        }

        @Override
        public String getDescription() {
            return this.getDisplayName();
        }

        @Override
        public boolean isRequired() {
            return this.required;
        }

        @Override
        public boolean validateValue(Object value) throws PropertyValidationException {
            if (this.isRequired() && value == null) {
                throw MissingPropertyException.forName(this.getName());
            } else if (value instanceof String) {
                if (!isValidLis200Address((String) value)) {
                    throw InvalidPropertyException.forNameAndValue(this.getName(), value);
                }
                return true;
            } else {
                throw InvalidPropertyException.forNameAndValue(this.getName(), value);
            }
        }

    }
}
