package com.energyict.mdc.upl.properties;

/**
 * Models a String value that actually represents a numerical value in HEX format.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-30 (13:35)
 */
public interface HexString {
    String getContent();

    int length();

    default boolean isEmpty() {
        return this.length() == 0;
    }
}