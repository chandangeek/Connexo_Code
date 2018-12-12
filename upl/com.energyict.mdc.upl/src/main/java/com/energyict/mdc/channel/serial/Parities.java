package com.energyict.mdc.channel.serial;

import java.util.stream.Stream;

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
        return Stream
                .of(values())
                .map(Parities::getParity)
                .toArray(String[]::new);
    }

    public static Parities valueFor(String strValue) {
        return Stream
                .of(values())
                .filter(each -> each.getParity().equals(strValue))
                .findAny()
                .orElse(null);
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

    public String getParity() {
        return parity;
    }

    public char getAbbreviation() {
        return abbreviation;
    }

    @Override
    public String toString() {
        return parity; //TODO find a way to get the translations in here
    }
}