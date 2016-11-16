package com.energyict.mdc.channels.serial;

import com.energyict.cpo.Environment;

/**
 * Provide predefined values for the used parity
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
            typedValues[i++] = parity.getParity();
        }
        return typedValues;
    }

    public String getParity() {
        return parity;
    }

    public char getAbbreviation() {
        return abbreviation;
    }

    public static Parities valueFor (String strValue) {
        for (Parities parity : values()) {
            if (parity.getParity().equals(strValue)) {
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

    @Override
    public String toString() {
        return Environment.getDefault().getTranslation(getParity());
    }
}
