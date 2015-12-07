/*
 * CodeDayType.java
 *
 * Created on 17 oktober 2003, 8:40
 */

package com.energyict.mdc.protocol.api.codetables;

import java.util.Calendar;
import java.util.List;

/**
 * implements a code day type.
 *
 * @author pasquien
 */
public interface CodeDayType {

    String getName();

    String getExternalName();

    int getId();

    /**
     * returns the code the receiver belongs to
     *
     * @return the code.
     */
    Code getCode();

    /**
     * Returns the list of code values defined for this day type
     *
     * @return a List of CodeDayTypeDef objects.
     */
    List<CodeDayTypeDef> getDefinitions();

    /**
     * Returns the code for a given time
     *
     * @param cal the date
     * @return the code.
     */
    int getCodeValue(Calendar cal);

}
