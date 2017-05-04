/*
 * ObisUtils.java
 *
 * Created on 20 mei 2005, 9:07
 */

package com.energyict.protocolimpl.base;

import com.energyict.obis.ObisCode;

/**
 *
 * @author  Koen
 */
public class ObisUtils {

    /** Creates a new instance of ObisUtils */
    public ObisUtils() {
    }


    static public boolean isManufacturerSpecific(ObisCode obisCode) {
        if ((obisCode.getA() == 0) &&
            (obisCode.getC() == 96) &&
            (obisCode.getD() == 99))
            return true;
        else
            return false;
    }
}
