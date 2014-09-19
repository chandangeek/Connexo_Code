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
        UNDEFINED(0, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.0.128.0.0.255")),
        KWH_SUPPLIED(1, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.1.128.0.0.255")),
        KVARH(2, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.2.128.0.0.255")),
        KQH(3, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.3.128.0.0.255")),
        V2H(4, BaseUnit.VOLTSQUAREHOUR, NO_SCALE, ObisCode.fromString("0.4.128.0.0.255")),
        I2H(5, BaseUnit.AMPERESQUAREHOUR, NO_SCALE, ObisCode.fromString("0.5.128.0.0.255")),
        DEACTIVATED(6, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.6.128.0.0.255")),
        UNDEFINED_UNIT(7, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.7.128.0.0.255")),
        VH(8, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.8.128.0.0.255")),
        IH(9, BaseUnit.AMPEREHOUR, NO_SCALE, ObisCode.fromString("0.9.128.0.0.255")),
        KVARH_IND(10, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.10.128.0.0.255")),
        KVARH_CAP(11, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.11.128.0.0.255")),
        KQH_IND(12, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.12.128.0.0.255")),
        KQH_CAP(13, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.13.128.0.0.255")),
        KWH_RECEIVED(14, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.14.128.0.0.255")),
        KVARH_IND_RECEIVED(15, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.15.128.0.0.255")),
        KVARH_CAP_RECEIVED(16, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.16.128.0.0.255")),
        VH_PHASE_A(17, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.17.128.0.0.255")),
        VH_PHASE_B(18, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.18.128.0.0.255")),
        VH_PHASE_C(19, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.19.128.0.0.255")),
        IH_PHASE_A(20, BaseUnit.AMPEREHOUR, NO_SCALE, ObisCode.fromString("0.20.128.0.0.255")),
        IH_PHASE_B(21, BaseUnit.AMPEREHOUR, NO_SCALE, ObisCode.fromString("0.21.128.0.0.255")),
        IH_PHASE_C(22, BaseUnit.AMPEREHOUR, NO_SCALE, ObisCode.fromString("0.22.128.0.0.255")),
        PFH(23, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.23.128.0.0.255")),
        HARMONIC_DISTORTION_HOUR(24, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.24.128.0.0.255")),
        KVAR_ALL_PHASES(25, BaseUnit.VOLTAMPEREHOUR, KILO_SCALE_FACTOR, ObisCode.fromString("0.25.128.0.0.255")),
        PFH_REVERSE(26, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.26.128.0.0.255")),
        PFH_PHASE_A(27, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.27.128.0.0.255")),
        PFH_PHASE_D(28, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.28.128.0.0.255")),
        PFH_PHASE_C(29, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.29.128.0.0.255")),
        THDH_VOLTAGE_PHASE_A(30, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.30.128.0.0.255")),
        THDH_VOLTAGE_PHASE_B(31, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.31.128.0.0.255")),
        THDH_VOLTAGE_PHASE_C(32, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.32.128.0.0.255")),
        THDH_CURRENT_PHASE_A(33, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.33.128.0.0.255")),
        THDH_CURRENT_PHASE_B(34, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.34.128.0.0.255")),
        THDH_CURRENT_PHASE_C(35, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.35.128.0.0.255")),
        MAX_VH_PHASE_A(36, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.36.128.0.0.255")),
        MAX_VH_PHASE_B(37, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.37.128.0.0.255")),
        MAX_VH_PHASE_C(38, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.38.128.0.0.255")),
        MIN_VH_PHASE_A(39, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.39.128.0.0.255")),
        MIN_VH_PHASE_B(40, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.40.128.0.0.255")),
        MIN_VH_PHASE_C(41, BaseUnit.VOLTHOUR, NO_SCALE, ObisCode.fromString("0.41.128.0.0.255")),
        NON_EXISTING_CHANNEL(99, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.99.128.0.0.255")),
        UNKNOWN(-1, BaseUnit.UNITLESS, NO_SCALE, ObisCode.fromString("0.x.128.0.0.255"));

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
            for (UnitMapping version : UnitMapping.values()) {
                if (version.getUnitCode() == statusCode) {
                    return version;
                }
            }
            return UnitMapping.UNKNOWN;
        }
    }
}