/*
 * CodeDayType.java
 *
 * Created on 17 oktober 2003, 8:40
 */

package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;

import java.util.Calendar;
import java.util.List;

/**
 * implements a code day type.
 *
 * @author pasquien
 */
public interface CodeDayType extends NamedBusinessObject {

    /**
     * returns the code the receiver belongs to
     *
     * @return the code.
     */
    public Code getCode();

    /**
     * Returns the list of code values defined for this day type
     *
     * @return a List of CodeDayTypeDef objects.
     */
    public List<CodeDayTypeDef> getDefinitions();

    /**
     * Returns the code for a given time
     *
     * @param cal the date
     * @return the code.
     * @throws BusinessException if no suitable rule is defined for the given date.
     */
    public int getCodeValue(Calendar cal) throws BusinessException;

}
