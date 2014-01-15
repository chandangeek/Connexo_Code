package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.BusinessObject;

/**
 * CodeDayTypeDef represents the code used for a certain part of a day.
 *
 * @author pasquien
 */
public interface CodeDayTypeDef extends BusinessObject {


    /**
     * Returns the day type the receiver belongs to.
     *
     * @return the day type
     */
    public CodeDayType getDayType();

    /**
     * returns the code
     *
     * @return the code
     */
    public int getCodeValue();

    /**
     * returns the time stamp from which this code is active
     *
     * @return the from time stamp
     */
    public int getTstampFrom();

}
