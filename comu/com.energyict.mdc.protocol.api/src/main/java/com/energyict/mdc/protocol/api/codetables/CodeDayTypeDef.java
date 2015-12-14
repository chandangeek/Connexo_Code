package com.energyict.mdc.protocol.api.codetables;

/**
 * CodeDayTypeDef represents the code used for a certain part of a day.
 *
 * @author pasquien
 */
public interface CodeDayTypeDef {

    /**
     * Returns the day type the receiver belongs to.
     *
     * @return the day type
     */
    CodeDayType getDayType();

    /**
     * returns the code
     *
     * @return the code
     */
    int getCodeValue();

    /**
     * returns the time stamp from which this code is active
     *
     * @return the from time stamp
     */
    int getTstampFrom();

}
