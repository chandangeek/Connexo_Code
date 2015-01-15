package com.energyict.mdc.io;

/**
 * Provide predefined values for the used parity.
 */
public enum Parities {
    NONE("parities_none", 'N'),
    ODD("parities_odd", 'O'),
    EVEN("parities_even", 'E'),
    MARK("parities_mark", 'M'),
    SPACE("parities_space", 'S');

    private final String parity;
    private final char abbreviation;

    Parities(String parity, char abbreviation) {
        this.parity = parity;
        this.abbreviation = abbreviation;
    }

    public static String[] getTypedValues() {
        String[] typedValues = new String[values().length];
        int i = 0;
        for (Parities parity : values()) {
            typedValues[i++] = parity.value();
        }
        return typedValues;
    }

    public String value() {
        return parity;
    }

    public char getAbbreviation() {
        return abbreviation;
    }

    public static Parities valueFor (String strValue) {
        for (Parities parity : values()) {
            if (parity.value().equals(strValue)) {
                return parity;
            }
        }
        return null;
    }

    public static Parities valueFor(Character abbreviation) {
        for (Parities parity : values()) {
            Character parityAbbreviation = parity.getAbbreviation();
            if (parityAbbreviation.toString().equalsIgnoreCase(abbreviation.toString())) {
                return parity;
            }
        }
        return null;
    }

}