/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;

class ObisCodeUtil {

    private static final int A_FIELD_ELECTRICITY = 1;
    private static final int A_FIELD_GAS = 7;

    private static final Matcher<Integer> ELECTRICITY_C_FIELD_ENERGY_RANGES = RangeMatcher.rangeMatcherFor(
            new Range(1, 10),   // Totals
            new Range(15, 20),  // Totals
            new Range(21, 30),   // L1
            new Range(35, 40),  // L1
            new Range(41, 50),  // L2
            new Range(55, 60),  // L2
            new Range(61, 70),  // L3
            new Range(75, 80)   // L3
    );

    private static final Matcher<Integer> ELECTRICITY_C_FIELD_CURRENT_VALUES = ItemMatcher.itemMatcherFor(11, 31, 51, 71, 88, 90, 91);
    private static final Matcher<Integer> ELECTRICITY_C_FIELD_VOLTAGE_VALUES = ItemMatcher.itemMatcherFor(12, 32, 52, 72, 89, 92);

    /**
     * Checks whether the ObisCode is related to an Electricity object.
     * <p>
     * <i>Check is made by checking if field A is equal to one {@link #A_FIELD_ELECTRICITY}</i>
     * </p>
     *
     * @param obisCode the obisCode to check
     * @return true if this is an Electricity related ObisCode, false otherwise
     */
    static boolean isElectricity(final ObisCode obisCode) {
        return obisCode != null && obisCode.getA() == A_FIELD_ELECTRICITY;
    }

    /**
     * Checks whether the ObisCode is related to an Gas object.
     * <p>
     * <i>Check is made by checking if field A is equal to one {@link #A_FIELD_GAS}</i>
     * </p>
     *
     * @param obisCode the obisCode to check
     * @return true if this is an Gas related ObisCode, false otherwise
     */
    public static boolean isGas(ObisCode obisCode) {
        return obisCode != null && obisCode.getA() == A_FIELD_GAS;
    }

    /**
     * Checks whether this given ObisCode is an Energy related object.
     * <p>
     * <i>Check is made by checking if this is:</i>
     * <ul>
     * <li>an Electricity ObisCode {@link #isElectricity(com.energyict.mdc.common.ObisCode)}</li>
     * <li>the C-field of the obisCode is in the range of {@link #ELECTRICITY_C_FIELD_ENERGY_RANGES}</li>
     * </ul>
     * </p>
     *
     * @param obisCode the obisCode to check
     * @return true if this is an Energy related ObisCode, false otherwise
     */
    static boolean isEnergy(final ObisCode obisCode) {
        return isElectricity(obisCode) && ELECTRICITY_C_FIELD_ENERGY_RANGES.match(obisCode.getC());
    }

    /**
     * Checks whether this given ObisCode is a Voltage related object.
     * <p>
     * <i>Check is made by checking if this is:</i>
     * <ul>
     * <li>an Electricity ObisCode {@link #isElectricity(com.energyict.mdc.common.ObisCode)}</li>
     * <li>the C-field of the obisCode is included in the set of {@link #ELECTRICITY_C_FIELD_VOLTAGE_VALUES}</li>
     * </ul>
     * </p>
     *
     * @param obisCode the obisCode to check
     * @return true if this is an Voltage related ObisCode, false otherwise
     */
    static boolean isVoltage(final ObisCode obisCode){
        return isElectricity(obisCode) && ELECTRICITY_C_FIELD_VOLTAGE_VALUES.match(obisCode.getC());
    }

    /**
     * Checks whether this given ObisCode is a Current related object.
     * <p>
     * <i>Check is made by checking if this is:</i>
     * <ul>
     * <li>an Electricity ObisCode {@link #isElectricity(com.energyict.mdc.common.ObisCode)}</li>
     * <li>the C-field of the obisCode is included in the set of {@link #ELECTRICITY_C_FIELD_CURRENT_VALUES}</li>
     * </ul>
     * </p>
     *
     * @param obisCode the obisCode to check
     * @return true if this is an Current related ObisCode, false otherwise
     */

    static boolean isCurrent(final ObisCode obisCode){
        return isElectricity(obisCode) && ELECTRICITY_C_FIELD_CURRENT_VALUES.match(obisCode.getC());
    }
}
