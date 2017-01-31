/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.ObisCode;

class TimeOfUseMapping {

    public static final int FIXED_NON_TOU_CODE = 0;
    private static final int MAX_DLMS_TOU_CODES = 63;

    /**
     * Not used private constructor
     */
    private TimeOfUseMapping() {
    }

    /**
     * Gets the TOU value from the given ObisCode. Mapping is made based on the E-field of the ObisCode
     * <p/>
     * (if E is higher than, {@link #FIXED_NON_TOU_CODE} will be returned
     *
     * @param obisCode the ObisCode to deduct the TOU code from
     * @return the TOU code
     */
    public static int getTimeOfUseFor(ObisCode obisCode) {
        if( obisCode != null && obisCode.getE() <= MAX_DLMS_TOU_CODES){
            if (ObisCodeUtil.isElectricity(obisCode)
                    || ObisCodeUtil.isGas(obisCode)) {
                return obisCode.getE();
            }
        }
        return FIXED_NON_TOU_CODE;
    }
}
