/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.UnitConversion;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

/**
 * @author sva
 * @since 9/02/2016 - 16:13
 */
public enum LoadControlMeasurementQuantity {

    NONE("00", "None", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.UNITLESS)),
    U_L1("80", "U,L1,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    U_L2("81", "U,L2,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    U_L3("82", "U,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    I_L1("83", "I,L1,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_L2("84", "I,L2,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_L3("85", "I,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.AMPERE)),
    P_POS_L1_L3("86", "+P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L1("87", "+P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L2("88", "+P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L3("89", "+P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L1_L3("8a", "-P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L1("8b", "-P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L2("8c", "-P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L3("8d", "-P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    Q_POS_L1_L3("8E", "+Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L1("8F", "+Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L2("90", "+Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L3("91", "+Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L1_L3("92", "-Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L1("93", "-Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L2("94", "-Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L3("95", "-Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    S_POS_L1_L3("96", "+S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L1("97", "+S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L2("98", "+S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L3("99", "+S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L1_L3("9A", "-S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L1("9B", "-S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L2("9C", "-S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L3("9D", "-S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    FREQUENCE_L1_L3_a("9e", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    FREQUENCE_L1_L3_b("9f", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    FREQUENCE_L1_L3_c("a0", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    PF_L1("a", "PF,L1,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    PF_L2("a9", "PF,L2,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    PF_L3("aa", "PF,L3,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    I_N("ab", "I,N,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_MAX("be", "Imax,maximum", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_SUM("bf", "Isum,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),

    INVALID("xx", "", new DecimalFormat("0000000.0"), Unit.getUndefined());

    private final String quantityCode;
    private final String description;
    private final DecimalFormat decimalFormat;
    private final Unit unit;

    LoadControlMeasurementQuantity(String quantityCode, String description, DecimalFormat decimalFormat, Unit unit) {
        this.quantityCode = quantityCode;
        this.description = description;
        this.decimalFormat = decimalFormat;
        this.unit = unit;
    }

    public static LoadControlMeasurementQuantity getLoadControlMeasurementQuantityForQuantityCode(String quantityCode) {
        for (LoadControlMeasurementQuantity loadControlMeasurementQuantity : values()) {
            if (loadControlMeasurementQuantity.getMeasurementQuantityCode().equals(quantityCode)) {
                return loadControlMeasurementQuantity;
            }
        }
        return INVALID;
    }

    public String getMeasurementQuantityCode() {
        return quantityCode;
    }

    public String getDescription() {
        return description;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * Format the threshold value according to this LoadControlMeasurementQuantity<br/>
     * Note: this involves:
     * <ul>
     * <li>unit conversion from the given unit towards the unit of this LoadControlMeasurementQuantity</li>
     * <li>formatting of threshold value (after unit conversion) to String, taking into account the number of digits
     * before and after the decimal point</li>
     * </ul>
     *
     * @param threshold the treshold value, expressed in the given unit
     * @param unit the unit in which the threshold value is expressed
     * @return the formatted threshold
     * @throws IOException
     */
    public String format(float threshold, Unit unit) throws IOException {
        try {
            Quantity quantity = UnitConversion.convertQuantity(new Quantity(threshold, unit), getUnit());
            threshold = quantity.getAmount().floatValue();
            return decimalFormat.format(threshold)
                    .replace(Character.toString(new DecimalFormatSymbols().getDecimalSeparator()), "");
        } catch (ArithmeticException e) {
            throw new IOException(e);
        }
    }

    public float format(String threshold) throws IOException {
        try {
            return decimalFormat.parse(threshold).floatValue();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}