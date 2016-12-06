package com.energyict.mdc.metering.impl;

import com.energyict.obis.ObisCode;

/**
 * The <i>TimeOfUse</i> is used to describe any attribution of the value to a specific TOU Bucket.
 * The TOU value can be directly mapped to the E-field of an ObisCode
 * (if it is an Electricity or Gas related object).
 * <p/>
 * <i>Note:</i> DLMS Cosem defines that E can range from 0-63
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 15:11
 */
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
