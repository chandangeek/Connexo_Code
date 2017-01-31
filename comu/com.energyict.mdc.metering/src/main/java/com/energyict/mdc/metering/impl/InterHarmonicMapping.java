/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.RationalNumber;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;

class InterHarmonicMapping {

    private static final Matcher<Integer> C_FIELD_VALUES = ItemMatcher.itemMatcherFor(12, 32, 52, 72, 92, 11, 31, 51, 71, 90, 91, 15, 35, 55, 75);
    private static final Matcher<Integer> D_FIELD_VALUES = ItemMatcher.itemMatcherFor(7, 24);
    private static final int MAX_DLMS_HARMONIC_CODES = 120;
    private static final long FIXED_HARMONIC_DENOMINATOR = 1;

    /**
     * Not used private constructor
     */
    private InterHarmonicMapping() {
    }

    public static RationalNumber getInterHarmonicFor(ObisCode obisCode) {
        if(obisCode != null && ObisCodeUtil.isElectricity(obisCode) &&
                obisCode.getE() > 0 &&
                obisCode.getE() <= MAX_DLMS_HARMONIC_CODES){
            if(C_FIELD_VALUES.match(obisCode.getC()) &&
                    D_FIELD_VALUES.match(obisCode.getD())){
                return new RationalNumber(obisCode.getE(), FIXED_HARMONIC_DENOMINATOR);
            }
        }
        return RationalNumber.NOTAPPLICABLE;
    }

    static Matcher<Integer> getcFieldValues() {
        return C_FIELD_VALUES;
    }

    static Matcher<Integer> getdFieldValues() {
        return D_FIELD_VALUES;
    }
}
