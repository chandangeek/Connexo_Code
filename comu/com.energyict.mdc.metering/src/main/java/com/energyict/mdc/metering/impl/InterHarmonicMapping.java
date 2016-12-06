package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.RationalNumber;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;

/**
 * Defines a mapping for the InterHarmonic value.
 * The Harmonic value can be directly mapped to the E-field of an ObisCode
 * (if it is an Electricity related object and matches the C and D field requirements).
 * An InterHarmonic is defined by a {@link RationalNumber} (<i>Numerator</i> / <i>Denominator</i>)
 * <p/>
 * <i>Note:</i> DLMS Cosem defines that E can range from 0-120
 * <p/>
 * <i>Note2:</i> The Single CIM defined Interharmonic (1/2) is not defined in DLMS COSEM, therefor the Denominator will always be "1"(see {@link #FIXED_HARMONIC_DENOMINATOR})
 *
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 15:05
 */
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
