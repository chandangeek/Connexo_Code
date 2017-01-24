package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class UnitField extends AbstractField<UnitField> {

    public static final int LENGTH = 1;
    private static final int KILO_SCALE_FACTOR = 3;
    private static final int NO_SCALE = 0;

    private int unitCode;
    private UnitMapping unitMapping;

    public UnitField() {
        this.unitMapping = UnitMapping.UNKNOWN;
    }

    public UnitField(UnitMapping unitMapping) {
        this.unitMapping = unitMapping;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(unitCode, LENGTH);
    }

    @Override
    public UnitField parse(byte[] rawData, int offset) throws ParsingException {
        unitCode = getIntFromBCD(rawData, offset, LENGTH);
        unitMapping = UnitMapping.fromUnitCode(unitCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getUnitCode() {
        return unitCode;
    }

    public Unit getEisUnit() {
        return unitMapping.getUnit();
    }

    public ObisCode getCorrespondingObisCode() {
        return unitMapping.getObisCode();
    }

    public boolean isCumulative() {
        return false;   // Quantities are measured during billing interval and thus never cumulative
    }

    public UnitMapping getUnitMapping() {
        return unitMapping;
    }

    public enum UnitMapping {
        UNDEFINED(0, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.0.0.0.255")),
        KWH_SUPPLIED(1, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.1.8.0.255")),
        KVARH(2, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.3.8.0.255")),
        KQH(3, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.11.8.0.255")),
        V2(4, BaseUnit.VOLTSQUARE, NO_SCALE, ObisCode.fromString("1.0.89.0.0.255")),
        I2(5, BaseUnit.AMPERESQUARE, NO_SCALE, ObisCode.fromString("1.0.88.0.0.255")),
        DEACTIVATED(6, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.1.0.0.0.255")),
        UNDEFINED_UNIT(7, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.2.0.0.0.255")),
        V(8, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.92.0.0.255")),
        I(9, BaseUnit.AMPERE, NO_SCALE, ObisCode.fromString("1.0.90.0.0.255")),
        KVARH_IND(10, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.4.8.0.255")),
        KVARH_CAP(11, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.1.4.8.0.255")),
        KQH_IND(12, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.12.8.0.255")),
        KQH_CAP(13, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.1.12.8.0.255")),
        KWH_RECEIVED(14, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.2.8.0.255")),
        KVARH_IND_RECEIVED(15, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.4.8.0.255")),
        KVARH_CAP_RECEIVED(16, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.1.4.8.0.255")),
        V_PHASE_A(17, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.32.0.0.255")),
        V_PHASE_B(18, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.52.0.0.255")),
        V_PHASE_C(19, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.72.0.0.255")),
        I_PHASE_A(20, BaseUnit.AMPERE, NO_SCALE, ObisCode.fromString("1.0.31.0.0.255")),
        I_PHASE_B(21, BaseUnit.AMPERE, NO_SCALE, ObisCode.fromString("1.0.51.0.0.255")),
        I_PHASE_C(22, BaseUnit.AMPERE, NO_SCALE, ObisCode.fromString("1.0.71.0.0.255")),
        PF(23, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.13.0.0.255")),
        HARMONIC_DISTORTION(24, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.92.4.124.255")),
        KVAH_ALL_PHASES(25, BaseUnit.VOLTAMPEREHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("1.0.9.8.0.255")),
        PF_REVERSE(26, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.84.0.0.255")),
        PF_PHASE_A(27, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.33.0.0.255")),
        PF_PHASE_B(28, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.53.0.0.255")),
        PF_PHASE_C(29, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.73.0.0.255")),
        THD_VOLTAGE_PHASE_A(30, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.32.4.124.255")),
        THD_VOLTAGE_PHASE_B(31, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.52.4.124.255")),
        THD_VOLTAGE_PHASE_C(32, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.72.4.124.255")),
        THD_CURRENT_PHASE_A(33, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.31.4.124.255")),
        THD_CURRENT_PHASE_B(34, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.51.4.124.255")),
        THD_CURRENT_PHASE_C(35, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.71.4.124.255")),
        MAX_V_PHASE_A(36, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.32.6.0.255")),
        MAX_V_PHASE_B(37, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.52.6.0.255")),
        MAX_V_PHASE_C(38, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.72.6.0.255")),
        MIN_V_PHASE_A(39, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.32.3.0.255")),
        MIN_V_PHASE_B(40, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.52.3.0.255")),
        MIN_V_PHASE_C(41, BaseUnit.VOLT, NO_SCALE, ObisCode.fromString("1.0.72.3.0.255")),
        NON_EXISTING_CHANNEL(99, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("1.0.99.0.0.255")),
        UNKNOWN(-1, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.0.0.0.0.0"));

        private final int unitCode;
        private final Unit unit;
        private final ObisCode obisCode;

        private UnitMapping(int unitCode, int baseUnitCode, int scale, ObisCode obisCode) {
            this.unitCode = unitCode;
            this.unit = Unit.get(baseUnitCode, scale);
            this.obisCode = obisCode;
        }

        public Unit getUnit() {
            return unit;
        }

        public int getUnitCode() {
            return unitCode;
        }

        public ObisCode getObisCode() {
            return obisCode;
        }

        public static UnitMapping fromUnitCode(int statusCode) {
            for (UnitMapping mapping : UnitMapping.values()) {
                if (mapping.getUnitCode() == statusCode) {
                    return mapping;
                }
            }
            return UnitMapping.UNKNOWN;
        }

        public static UnitMapping fromObisCode(ObisCode obisCode) {
            for (UnitMapping mapping : UnitMapping.values()) {
                if (mapping.getObisCode().equals(obisCode)) {
                    return mapping;
                }
            }
            return UnitMapping.UNKNOWN;
        }
    }
}