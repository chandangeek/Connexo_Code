package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author sva
 * @since 9/02/2016 - 16:13
 */
public enum LoadControlMeasurementQuantity {

    NONE("00", "None", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.UNITLESS, -1)),
    U_L1("80", "U,L1,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT, -1)),
    U_L2("81", "U,L2,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT, -1)),
    U_L3("82", "U,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT, -1)),
    I_L1("83", "I,L1,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE, -3)),
    I_L2("84", "I,L2,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE, -3)),
    I_L3("85", "I,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.AMPERE, -3)),
    P_POS_L1_L3("86", "+P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_POS_L1("87", "+P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_POS_L2("88", "+P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_POS_L3("89", "+P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_NEG_L1_L3("8a", "-P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_NEG_L1("8b", "-P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_NEG_L2("8c", "-P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    P_NEG_L3("8d", "-P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT)),
    Q_POS_L1_L3("8E", "+Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_POS_L1("8F", "+Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_POS_L2("90", "+Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_POS_L3("91", "+Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_NEG_L1_L3("92", "-Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_NEG_L1("93", "-Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_NEG_L2("94", "-Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    Q_NEG_L3("95", "-Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
    S_POS_L1_L3("96", "+S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_POS_L1("97", "+S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_POS_L2("98", "+S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_POS_L3("99", "+S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_NEG_L1_L3("9A", "-S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_NEG_L1("9B", "-S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_NEG_L2("9C", "-S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    S_NEG_L3("9D", "-S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE)),
    FREQUENCE_L1_L3_a("9e", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ, -2)),
    FREQUENCE_L1_L3_b("9f", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ, -2)),
    FREQUENCE_L1_L3_c("a0", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ, -2)),
    LF_L1_L3("a8", "LF,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS, -2)),
    LF_L1("a9", "LF,L1,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS, -2)),
    LF_L2("a9", "LF,L2,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS, -2)),
    LF_L3("aa", "LF,L3,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS, -2)),
    I_N("ab", "I,N,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE, -3)),
    I_MAX("be", "Imax,maximum", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE, -3)),
    I_SUM("be", "Isum,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE, -3)),

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

    public String getMeasurementQuantityCode() {
        return quantityCode;
    }

    public String getDescription() {
        return description;
    }

    public Unit getUnit() {
        return unit;
    }

    public String format(float threshold) {
        return decimalFormat.format(threshold)
                .replace(Character.toString(new DecimalFormatSymbols().getDecimalSeparator()), "");
    }

    public static LoadControlMeasurementQuantity getLoadControlMeasurementQuantity(String quantityCode) {
        for (LoadControlMeasurementQuantity loadControlMeasurementQuantity : values()) {
            if (loadControlMeasurementQuantity.getMeasurementQuantityCode().equals(quantityCode)) {
                return loadControlMeasurementQuantity;
            }
        }
        return INVALID;
    }
}