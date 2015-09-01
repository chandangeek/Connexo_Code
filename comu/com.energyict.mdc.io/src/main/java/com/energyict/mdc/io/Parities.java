package com.energyict.mdc.io;

import java.util.Arrays;

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
        return Arrays.stream(values()).map(Parities::value).toArray(String[]::new);
    }

    public String value() {
        return parity;
    }

    public char getAbbreviation() {
        return abbreviation;
    }

    public static Parities valueFor (String strValue) {
        return Arrays.stream(values()).filter(x -> x.value().equals(strValue)).findFirst().orElse(null);
    }

    public static Parities valueFor(Character abbreviation) {
        return Arrays.stream(values()).filter(x -> Character.valueOf(x.getAbbreviation()).toString().equalsIgnoreCase(abbreviation.toString())).findFirst().orElse(null);
    }

}