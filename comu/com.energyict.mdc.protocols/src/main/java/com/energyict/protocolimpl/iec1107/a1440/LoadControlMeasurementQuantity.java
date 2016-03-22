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

    NONE("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "00", "None", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.UNITLESS)),
    U_L1("0.0.0.12.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "80", "U,L1,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    U_L2("0.0.0.12.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "81", "U,L2,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    U_L3("0.0.0.12.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "82", "U,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.VOLT)),
    I_L1("0.0.0.12.1.1.4.0.0.0.0.0.0.0.128.0.5.0", "83", "I,L1,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_L2("0.0.0.12.1.1.4.0.0.0.0.0.0.0.64.0.5.0", "84", "I,L2,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_L3("0.0.0.12.1.1.4.0.0.0.0.0.0.0.32.0.5.0", "85", "I,L3,total", new DecimalFormat("0000000.0"), Unit.get(BaseUnit.AMPERE)),
    P_POS_L1_L3("0.0.0.12.1.1.37.0.0.0.0.0.0.0.0.0.38.0", "86", "+P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L1("0.0.0.12.1.1.37.0.0.0.0.0.0.0.128.0.38.0", "87", "+P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L2("0.0.0.12.1.1.37.0.0.0.0.0.0.0.64.0.38.0", "88", "+P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_POS_L3("0.0.0.12.1.1.37.0.0.0.0.0.0.0.32.0.38.0", "89", "+P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L1_L3("0.0.0.12.19.1.37.0.0.0.0.0.0.0.0.0.38.0", "8a", "-P,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L1("0.0.0.12.19.1.37.0.0.0.0.0.0.0.128.0.38.0", "8b", "-P,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L2("0.0.0.12.19.1.37.0.0.0.0.0.0.0.64.0.38.0", "8c", "-P,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    P_NEG_L3("0.0.0.12.19.1.37.0.0.0.0.0.0.0.32.0.38.0", "8d", "-P,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.WATT, 3)),
    Q_POS_L1_L3("0.0.0.12.1.1.37.0.0.0.0.0.0.0.0.0.63.0", "8E", "+Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L1("0.0.0.12.1.1.37.0.0.0.0.0.0.0.128.0.63.0", "8F", "+Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L2("0.0.0.12.1.1.37.0.0.0.0.0.0.0.64.0.63.0", "90", "+Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_POS_L3("0.0.0.12.1.1.37.0.0.0.0.0.0.0.32.0.63.0", "91", "+Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L1_L3("0.0.0.12.19.1.37.0.0.0.0.0.0.0.0.0.63.0", "92", "-Q,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L1("0.0.0.12.19.1.37.0.0.0.0.0.0.0.128.0.63.0", "93", "-Q,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L2("0.0.0.12.19.1.37.0.0.0.0.0.0.0.64.0.63.0", "94", "-Q,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    Q_NEG_L3("0.0.0.12.19.1.37.0.0.0.0.0.0.0.32.0.63.0", "95", "-Q,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3)),
    S_POS_L1_L3("0.2.0.6.1.1.37.0.0.0.0.0.0.0.0.0.61.0", "96", "+S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L1("0.2.0.6.1.1.37.0.0.0.0.0.0.0.128.0.61.0", "97", "+S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L2("0.2.0.6.1.1.37.0.0.0.0.0.0.0.64.0.61.0", "98", "+S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_POS_L3("0.2.0.6.1.1.37.0.0.0.0.0.0.0.32.0.61.0", "99", "+S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L1_L3("0.2.0.6.19.1.37.0.0.0.0.0.0.0.0.0.61.0", "9A", "-S,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L1("0.2.0.6.19.1.37.0.0.0.0.0.0.0.128.0.61.0", "9B", "-S,L1,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L2("0.2.0.6.19.1.37.0.0.0.0.0.0.0.64.0.61.0", "9C", "-S,L2,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    S_NEG_L3("0.2.0.6.19.1.37.0.0.0.0.0.0.0.32.0.61.0", "9D", "-S,L3,T0", new DecimalFormat("00000.000"), Unit.get(BaseUnit.VOLTAMPERE, 3)),
    FREQUENCE_L1_L3_a("0.0.0.12.0.1.15.0.0.0.0.0.0.0.0.0.33.0", "9e", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    FREQUENCE_L1_L3_b("0.0.0.12.0.1.15.0.0.0.0.0.0.0.0.0.33.0", "9f", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    FREQUENCE_L1_L3_c("0.0.0.12.0.1.15.0.0.0.0.0.0.0.0.0.33.0", "a0", "f", new DecimalFormat("000000.00"), Unit.get(BaseUnit.HERTZ)),
    PF_L1("0.0.0.12.0.1.38.0.0.0.0.0.0.0.128.0.0.0", "a", "PF,L1,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    PF_L2("0.0.0.12.0.1.38.0.0.0.0.0.0.0.64.0.0.0", "a9", "PF,L2,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    PF_L3("0.0.0.12.0.1.38.0.0.0.0.0.0.0.32.0.0.0", "aa", "PF,L3,total", new DecimalFormat("000000.00"), Unit.get(BaseUnit.UNITLESS)),
    I_N("0.0.0.12.1.1.4.0.0.0.0.0.0.0.16.0.5.0", "ab", "I,N,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_MAX("0.0.0.0.0.1.4.0.0.0.0.0.0.0.16.0.5.0", "be", "Imax,maximum", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),
    I_SUM("0.26.0.0.0.1.4.0.0.0.0.0.0.0.0.0.5.0", "bf", "Isum,total", new DecimalFormat("00000.000"), Unit.get(BaseUnit.AMPERE)),

    INVALID("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "xx", "", new DecimalFormat("0000000.0"), Unit.getUndefined());

    private final String readingType;
    private final String quantityCode;
    private final String description;
    private final DecimalFormat decimalFormat;
    private final Unit unit;

    LoadControlMeasurementQuantity(String readingType, String quantityCode, String description, DecimalFormat decimalFormat, Unit unit) {
        this.readingType = readingType;
        this.quantityCode = quantityCode;
        this.description = description;
        this.decimalFormat = decimalFormat;
        this.unit = unit;
    }

    public String getReadingType() {
        return readingType;
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

    public static LoadControlMeasurementQuantity getLoadControlMeasurementQuantityForReadingType(String readingType) {
        for (LoadControlMeasurementQuantity loadControlMeasurementQuantity : values()) {
            if (loadControlMeasurementQuantity.getReadingType().equals(readingType)) {
                return loadControlMeasurementQuantity;
            }
        }
        return INVALID;
    }

    public static LoadControlMeasurementQuantity getLoadControlMeasurementQuantityForQuantityCode(String quantityCode) {
        for (LoadControlMeasurementQuantity loadControlMeasurementQuantity : values()) {
            if (loadControlMeasurementQuantity.getMeasurementQuantityCode().equals(quantityCode)) {
                return loadControlMeasurementQuantity;
            }
        }
        return INVALID;
    }
}