package com.elster.protocolimpl.lis200;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LIS200Utils {

    /**
     * @param objectAddress is the Lis200 address to check
     * @return true if objectAddress is a syntactically correct address,
     *         otherwise returns false
     */
    public static boolean isValidLis200Address(String objectAddress) {
        try {
            Lis200Address.from(objectAddress);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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

    private static class Lis200Address implements com.energyict.mdc.upl.properties.Lis200Address{
        private final int c, d, e;

        static Lis200Address from(String objectAddress) {
            String[] addressPart = objectAddress.split(":");
            if (addressPart.length != 2) {
                throw new IllegalArgumentException();
            }
            try {
                int c = Integer.parseInt(addressPart[0]);
                String[] addressPart2 = addressPart[1].split("[.]");
                if (addressPart2.length != 2) {
                    throw new IllegalArgumentException();
                } else {
                    int d = Integer.parseInt(addressPart2[0]);
                    int e = Integer.parseInt(addressPart2[1]);
                    return new Lis200Address(c, d, e);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException();
            }
        }

        private Lis200Address(int c, int d, int e) {
            this.c = c;
            this.d = d;
            this.e = e;
        }

        @Override
        public String toString() {
            return this.c + ":" + this.d + "." + this.e;
        }

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

        @Override
        public Optional<?> getDefaultValue() {
            return Optional.empty();
        }

        @Override
        public PropertySpecPossibleValues getPossibleValues() {
            return new NoPossibleValues();
        }

        @Override
        public boolean supportsMultiValues() {
            return false;
        }

        @Override
        public com.energyict.mdc.upl.properties.ValueFactory getValueFactory() {
            return new ValueFactory();
        }

    }

    static final class ValueFactory implements com.energyict.mdc.upl.properties.ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            return Lis200Address.from(stringValue);
        }

        @Override
        public String toStringValue(Object object) {
            return object.toString();
        }

        @Override
        public String getValueTypeName() {
            return com.energyict.mdc.upl.properties.Lis200Address.class.getName();
        }

        @Override
        public Object valueToDatabase(Object object) {
            return this.toStringValue(object);
        }

        @Override
        public Object valueFromDatabase(Object databaseValue) {
            return this.fromStringValue((String) databaseValue);
        }
    }

    private static class NoPossibleValues implements PropertySpecPossibleValues {
        @Override
        public PropertySelectionMode getSelectionMode() {
            return PropertySelectionMode.NONE;
        }

        @Override
        public List<?> getAllValues() {
            return Collections.emptyList();
        }

        @Override
        public boolean isExhaustive() {
            return true;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public Object getDefault() {
            return null;
        }
    }

}
